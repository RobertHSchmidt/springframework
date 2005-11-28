/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.webflow.jsf;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.FlowLocator;

/**
 * A JSF-specific subclass of
 * {@link org.springframework.webflow.execution.FlowExecutionManager} which is
 * delegated to by Web Flow's
 * {@link org.springframework.webflow.jsf.FlowNavigationHandler} and
 * {@link org.springframework.webflow.jsf.FlowPhaseListener}. The latter
 * classes will expect to find a configured instance of this class in the web
 * application context accessible through
 * {@link FacesContextUtils#getWebApplicationContext(javax.faces.context.FacesContext)}
 * as the bean named {@link #BEAN_NAME}.
 * 
 * @author Colin Sampaleanu
 * @author Craig McClanahan
 * @author Keith Donald
 */
public class JsfFlowExecutionManager extends FlowExecutionManager {

	/**
	 * Bean name under which we will find the configured instance of the
	 * {@link JsfFlowExecutionManager} to be used for determining what logical
	 * actions to undertake.
	 */
	public static final String BEAN_NAME = "flowExecutionManager";

	/**
	 * Prefix on a logical outcome value that identifies a logical outcome as
	 * the identifier for a web flow that should be entered.
	 */
	protected static final String FLOW_ID_PREFIX = "flowId:";

	/**
	 * Resolves view selection names to JSF view IDs.
	 */
	private DefaultViewIdResolver viewIdResolver = new DefaultViewIdResolver();

	/**
	 * Create a new flow execution manager using the specified flow locator for
	 * loading Flow definitions.
	 * @param flowLocator the flow locator to use
	 */
	public JsfFlowExecutionManager(FlowLocator flowLocator) {
		super(flowLocator);
	}

	/**
	 * Allows the standard view id resolver to be overriden
	 * @param viewIdResolver the new view id resolver
	 */
	public void setViewIdResolver(DefaultViewIdResolver viewIdResolver) {
		this.viewIdResolver = viewIdResolver;
	}

	/**
	 * Return <code>true</code> if the current request is asking for the
	 * creation of a new flow. The default implementation examines the logical
	 * outcome to see if it starts with the prefix specified by
	 * {@link #FLOW_ID_PREFIX}.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowLaunchRequest(FacesContext context, String fromAction, String outcome) {
		if (outcome == null) {
			return false;
		}
		return outcome.startsWith(FLOW_ID_PREFIX);
	}

	/**
	 * Create a new flow execution for the current request. Proceed until a
	 * <code>ViewSelection</code> is returned describing the next view that
	 * should be rendered.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @return the selected starting view
	 */
	public ViewSelection launchFlowExecution(FacesContext context, String fromAction, String outcome) {
		String flowId = getRequiredFlowId(outcome);
		ExternalContext jsfContext = createExternalContext(context, fromAction, outcome);
		FlowExecution flowExecution = createFlowExecution(getFlowLocator().getFlow(flowId));
		ViewSelection selectedView = flowExecution.start(extractStateId(jsfContext), jsfContext);
		Serializable flowExecutionId = manageStorage(null, flowExecution, jsfContext);
		return prepareSelectedView(selectedView, flowExecutionId, flowExecution);
	}

	protected ExternalContext createExternalContext(FacesContext context, String fromAction, String outcome) {
		return new JsfExternalContext(context, fromAction, outcome);
	}

	/**
	 * Returns the flow id in the submitted outcome string. Subclasses may
	 * override.
	 * @param outcome the jsf outcome
	 * @return the flow id
	 * @throws IllegalArgumentException if no flow id was found
	 */
	protected String getRequiredFlowId(String outcome) throws IllegalArgumentException {
		// strip off the webflow prefix, leaving the flowId to launch
		String flowId = outcome.substring(FLOW_ID_PREFIX.length());
		Assert.hasText(flowId, "The id of the flow to launch was not provided in the outcome string: programmer error");
		return flowId;
	}

	/**
	 * Return <code>true</code> if there is an existing flow execution in
	 * progress for the current request. Implementors must ensure that the
	 * algorithm used to makes this determination matches the determination that
	 * will be made by the <code>FlowExecutionManager</code> that is being
	 * used. The default implementation looks for a request parameter named by
	 * {@link org.springframework.webflow.execution.FlowExecutionManager}.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowExecutionParticipationRequest(FacesContext context, String fromAction, String outcome) {
		boolean executionBound = FlowExecutionHolder.getFlowExecution() == null ? false : true;
		boolean flowExecutionIdPresent = context.getExternalContext().getRequestParameterMap().containsKey(
				FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER);
		// assert an invariant, just to be safe
		Assert.isTrue(executionBound == flowExecutionIdPresent,
				"The flow execution bound to the current thread context must match "
						+ "the existence of a _flowExecutionId request attribute");
		return executionBound;
	}

	/**
	 * JSF navigation-focused method which resumes an active flow execution for
	 * the current request. It is expected that the flow execution itself has
	 * already been restored into the current thread context available through
	 * {@link FlowExecutionHolder}.
	 * <p>
	 * Proceed until a <code>ViewSelection</code> is returned describing the
	 * next view that should be rendered.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @return the selected next (or ending) view
	 */
	public ViewSelection resumeFlowExecution(FacesContext context, String fromAction, String outcome) {
		Serializable flowExecutionId = FlowExecutionHolder.getFlowExecutionId();
		FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
		ExternalContext jsfContext = createExternalContext(context, fromAction, outcome);
		ViewSelection selectedView = signalEventIn(flowExecution, jsfContext);
		flowExecutionId = manageStorage(flowExecutionId, flowExecution, jsfContext);
		return prepareSelectedView(selectedView, flowExecutionId, flowExecution);
	}

	/*
	 * Overrides the default manageStorage implementation to provide support for
	 * two-phase FlowExecution saves.
	 * @see org.springframework.webflow.execution.FlowExecutionManager#manageStorage(java.io.Serializable,
	 * org.springframework.webflow.execution.FlowExecution,
	 * org.springframework.webflow.ExternalContext)
	 */
	protected Serializable manageStorage(Serializable flowExecutionId, FlowExecution flowExecution,
			ExternalContext context) {
		if (flowExecution.isActive()) {
			if (getStorage().supportsTwoPhaseSave()) {
				// just generate the flow execution id, an actual save will be
				// done after response rendering
				// see {@link #saveFlowExecutionIfNecessary()}
				flowExecutionId = getStorage().generateId(null);
				FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecution, context, false);
			}
			else {
				// two-phase save not supported, save out in one step
				flowExecutionId = saveFlowExecution(flowExecutionId, flowExecution, context);
				FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecution, context, true);
			}
		}
		else {
			if (flowExecutionId != null) {
				removeFlowExecution(flowExecutionId, flowExecution, context);
				flowExecutionId = null;
			}
		}
		return flowExecutionId;
	}

	/**
	 * Saves the current thread-bound FlowExecution out to storage if necessary;
	 * specifically, only if it actually exsists, and only if saving is a two
	 * phase process. In this case, the first part of a save happens after event
	 * processing when a storage ID is generated, the second part happens here
	 * and actually puts the execution into storage after response rendering
	 * with that generated
	 */
	public void saveFlowExecutionIfNecessary() {
		FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
		if (flowExecution != null && !FlowExecutionHolder.isFlowExecutionSaved()) {
			Serializable flowExecutionId = FlowExecutionHolder.getFlowExecutionId();
			Assert.notNull(flowExecutionId,
					"Flow execution storage id must have been pre-generated to complete two-phase save to storage");
			getStorage().saveWithGeneratedId(flowExecutionId, flowExecution, FlowExecutionHolder.getExternalContext());
			FlowExecutionHolder.setFlowExecutionSaved(true);
			flowExecution.getListeners().fireSaved(flowExecution, flowExecutionId);
			if (logger.isDebugEnabled()) {
				logger.debug("Saved flow execution out to storage with previously generated id: '" + flowExecutionId
						+ "'");
			}
		}
	}

	/**
	 * Render the view specified by this <code>ViewSelection</code>, after
	 * exposing any model data it includes.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param selectedView <code>ViewSelection</code> for the view to render
	 */
	public void renderView(FacesContext context, String fromAction, String outcome, ViewSelection selectedView) {
		// Expose model data specified in the descriptor
		try {
			context.getExternalContext().getRequestMap().putAll(selectedView.getModel());
		}
		catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll remove after it's fixed in MyFaces
			Map requestMap = context.getExternalContext().getRequestMap();
			Iterator it = selectedView.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				requestMap.put(entry.getKey(), entry.getValue());
			}
		}
		// stay on the same view if requested
		if (selectedView.getViewName() == null) {
			return;
		}
		// create the specified view so that it can be rendered
		ViewHandler handler = context.getApplication().getViewHandler();
		UIViewRoot view = handler.createView(context, viewIdResolver.resolveViewName(selectedView.getViewName()));
		context.setViewRoot(view);
	}

	/**
	 * Responsible for restoring (loading) the flow execution if the appropriate
	 * flow execution id parameter is found in the faces context request map.
	 * <p>
	 * Normally called by the phase listener.
	 * <p>
	 * The flow execution (if any) is also loaded into the FlowExecutionHolder.
	 * @param context <code>FacesContext</code> for the current request
	 */
	public void restoreFlowExecution(FacesContext context) {
		Map parameters = context.getExternalContext().getRequestParameterMap();
		if (parameters.containsKey(getFlowExecutionIdParameterName())) {
			Serializable id = (Serializable)parameters.get(getFlowExecutionIdParameterName());
			// note the event will be replaced during navigation
			JsfExternalContext jsfContext = new JsfExternalContext(context);
			FlowExecution flowExecution = loadFlowExecution(id, jsfContext);
			FlowExecutionHolder.setFlowExecution(id, flowExecution, jsfContext, false);
		}
	}
    
	/**
     *  Extract the event id for the JSF case. The superclass method looks for an _eventId
     *  request param, while for JSF we need to treat the JSF outcome (that was passed to
     *  our navigation handler) as the event id. The Outcome is actually the static action
     *  that the JSF page submitted.
     *  
     * @param context the context in which the external user event occured
     * @return the event id
	 */
    protected String extractEventId(ExternalContext context) throws IllegalArgumentException {
        
        JsfExternalContext jsfContext = (JsfExternalContext) context;
        
        String eventId = jsfContext.getOutcome();
        Assert.hasText(eventId, "No eventId could be obtained: make sure the client provides "
                + "the event id in the form of a JSF static action; "
                + "the parameters provided for this request were:"
                + StylerUtils.style(context.getRequestParameterMap()));
        if (eventId.equals(getNotSetEventIdParameterMarker())) {
            throw new IllegalArgumentException("The received eventId was the 'not set' marker '"
                    + getNotSetEventIdParameterMarker()
                    + "' -- this is likely a client view (jsp, etc) configuration error --" + "the '"
                    + getEventIdParameterName() + "' parameter must be set to a valid event");
        }
        return eventId;
    }
    
	/**
	 * Return the JsfFlowExecutionManager from a known location, as a bean
	 * called FlowNavigationHandlerStrategy.BEAN_NAME in the web application
	 * context accessible from FacesContextUtils.getWebApplicationContext.
	 * <p>
	 * This value should be cached.
	 * @param context <code>FacesContext</code> for the current request
	 */
	public static JsfFlowExecutionManager getFlowExecutionManager(FacesContext context) {
		WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(context);
		return (JsfFlowExecutionManager)wac.getBean(JsfFlowExecutionManager.BEAN_NAME, JsfFlowExecutionManager.class);
	}

	// standard default view id resolver which uses the web flow view name as
	// the jsf view id
	static class DefaultViewIdResolver {
		public String resolveViewName(String viewName) {
			return viewName;
		}
	}
   
}
