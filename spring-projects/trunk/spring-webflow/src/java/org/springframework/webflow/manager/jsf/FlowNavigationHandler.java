/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.manager.jsf;

import java.util.Iterator;
import java.util.Map;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.manager.EmptyFlowExecutionListenerLoader;
import org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor;

/**
 * An implementation of a JSF <code>NavigationHandler</code> that provides
 * integration with Spring Web Flow. It delegates handling to the standard
 * implementation when there is no current flow.
 * <ul>
 * <li>If a flow execution is <strong>not</strong> currently in progress:
 * <ul>
 * <li>If the specified logical outcome is <strong>not</strong> of the form
 * <em>flowId:xxx</em>, delegate to the standard
 * <code>NavigationHandler</code> implementation and return.</li>
 * <li>If the specified logical outcome <strong>is</strong> of the form
 * <em>flowId:xxx</em>, look up the corresponding
 * {@link org.springframework.webflow.Flow} definition with that id and launch a
 * new flow execution in the starting state. Record state information to
 * indicate that this flow is in progress.</li>
 * </ul>
 * </li>
 * <li>If a flow execution <strong>is</strong> currently in progress:
 * <ul>
 * <li>Load the reference to the current in-progress flow execution using the
 * submitted <em>_flowExecutionId</em>.</li>
 * <li>Resume the flow execution by signaling what action (event) the user took
 * in the current state.
 * <li>Wait for state event processing to complete, which happens when a
 * <code>ViewSelection</code> is returned selecting the next view to be
 * rendered.</li>
 * <li>Cause navigation to render the requested view.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowNavigationHandler extends NavigationHandler {

	/**
	 * The service name of the default {@link FlowLocator} implementation
	 * exported in the Spring Web Application Context.
	 */
	private static final String FLOW_LOCATOR_BEAN_NAME = "flowLocator";

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client. Defaults to a bean factory based lookup
	 * strategy.
	 */
	private FlowLocator flowLocator;

	/**
	 * The strategy for loading flow execution listeners that should monitor
	 * active flow executions.
	 */
	private FlowExecutionListenerLoader listenerLoader = new EmptyFlowExecutionListenerLoader();

	/**
	 * A helper for extracting parameters needed by this flow navigation
	 * handler.
	 */
	private FlowExecutionManagerParameterExtractor parameterExtractor = new FlowExecutionManagerParameterExtractor();

	/**
	 * Resolves selected Web Flow view names to JSF view ids.
	 */
	private ViewIdResolver viewIdResolver = new DefaultViewIdResolver();

	/**
	 * The standard <code>NavigationHandler</code> implementation that we are
	 * wrapping.
	 */
	private NavigationHandler handlerDelegate;

	/**
	 * Create a new {@link FlowNavigationHandler}, wrapping the specified
	 * standard navigation handler implementation.
	 * @param handlerDelegate Standard <code>NavigationHandler</code> we are
	 * wrapping
	 */
	public FlowNavigationHandler(NavigationHandler handlerDelegate) {
		this.handlerDelegate = handlerDelegate;
	}

	/**
	 * Returns the flow locator to use for lookup of flow definitions to
	 * execute.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flow definitions to execute.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the listener loader instance to be used by this flow execution
	 * manager.
	 */
	public FlowExecutionListenerLoader getListenerLoader() {
		return listenerLoader;
	}

	/**
	 * Sets the listener loader instance to be used by this flow execution
	 * manager.
	 */
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		this.listenerLoader = listenerLoader;
	}

	/**
	 * Returns the parameter extractor used by this navigation handler.
	 */
	public FlowExecutionManagerParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	/**
	 * Sets the parameter extractor to use.
	 */
	public void setParameterExtractor(FlowExecutionManagerParameterExtractor parameterExtractor) {
		this.parameterExtractor = parameterExtractor;
	}

	/**
	 * Returns the JSF view id resolver used by this navigation handler.
	 */
	public ViewIdResolver getViewIdResolver() {
		return viewIdResolver;
	}

	/**
	 * Sets the JSF view id resolver used by this navigation handler.
	 */
	public void setViewIdResolver(ViewIdResolver viewIdResolver) {
		this.viewIdResolver = viewIdResolver;
	}

	/**
	 * Handle the navigation request implied by the specified parameters.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
		JsfExternalContext context = new JsfExternalContext(facesContext, fromAction, outcome);
		FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
		if (holder != null) {
			// a flow execution has already been restored, signal an event in it
			FlowExecution flowExecution = holder.getFlowExecution();
			String eventId = parameterExtractor.extractEventId(context);
			ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
			renderView(selectedView, facesContext);
		}
		else {
			String flowId = parameterExtractor.extractFlowId(context);
			if (StringUtils.hasText(flowId)) {
				// a flow execution launch has been requested, start it
				Flow flow = getFlowLocator(facesContext).getFlow(flowId);
				FlowExecution flowExecution = createFlowExecution(flow, facesContext);
				ViewSelection selectedView = flowExecution.start(context);
				FlowExecutionHolderUtils.setFlowExecutionHolder(new FlowExecutionHolder(flowExecution), facesContext);
				renderView(selectedView, facesContext);
			}
			else {
				// neither has happened, delegate to std navigation handler
				handlerDelegate.handleNavigation(facesContext, fromAction, outcome);
			}
		}
	}

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine
	 * this if they wish to use a specialized FlowExecution implementation
	 * class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow, FacesContext context) {
		FlowExecution flowExecution = new FlowExecutionImpl(flow, listenerLoader.getListeners(flow));
		if (logger.isDebugEnabled()) {
			logger.debug("Created a new flow execution for flow definition '" + flow.getId() + "'");
		}
		return flowExecution;
	}

	/**
	 * Render the view specified by this <code>ViewSelection</code>, after
	 * exposing any model data it includes.
	 * @param facesContext <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param selectedView <code>ViewSelection</code> for the view to render
	 */
	public void renderView(ViewSelection selectedView, FacesContext facesContext) {
		putInto(facesContext.getExternalContext().getRequestMap(), selectedView.getModel());
		// stay on the same view if requested
		if (selectedView.getViewName() == null) {
			return;
		}
		// create the specified view so that it can be rendered
		ViewHandler handler = facesContext.getApplication().getViewHandler();
		UIViewRoot view = handler.createView(facesContext, viewIdResolver.resolveViewId(selectedView.getViewName()));
		facesContext.setViewRoot(view);
	}

	/**
	 * Utility method needed needed only because we can not rely on JSF
	 * RequestMap supporting Map's putAll method. Tries putAll, falls back to
	 * individual adds
	 * @param targetMap the target map to add the model data to
	 * @param map the model data to add to the target map
	 */
	private void putInto(Map targetMap, Map map) {
		try {
			targetMap.putAll(map);
		}
		catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll remove after it's fixed in MyFaces
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				targetMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Lookup the flow locator service by querying the application context for a
	 * bean with name {@link #FLOW_LOCATOR_BEAN_NAME}.
	 * @param context the faces context
	 * @return the flow locator
	 */
	protected FlowLocator getFlowLocator(FacesContext context) {
		if (flowLocator == null) {
			flowLocator = (FlowLocator)FacesContextUtils.getRequiredWebApplicationContext(context).getBean(
					FLOW_LOCATOR_BEAN_NAME);
		}
		return flowLocator;
	}

	/**
	 * Standard default view id resolver which uses the web flow view name as
	 * the jsf view id
	 */
	public static class DefaultViewIdResolver implements ViewIdResolver {
		public String resolveViewId(String viewName) {
			return viewName;
		}
	}
}