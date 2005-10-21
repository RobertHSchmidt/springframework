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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.FlowExecutionStorage;
import org.springframework.webflow.execution.TransactionSynchronizer;

/**
 * Note, this is the original strategy class for jsf flow navigation, but
 * (probably temporarilly) it's been made to derive from FlowExecutionManager.
 * There are some methods at the end that should be moved up to
 * FlowExecutionManager.
 * 
 * <p>
 * Strategy methods for {@link FlowNavigationHandler}. Because a JSF
 * <code>NavigationHandler</code> must be registered directly with the JSF
 * runtime, the implementation class cannot be customized in the typical fashion
 * for a Spring-based application. Therefore, decisions that would typically be
 * placed in hook methods for a specialzed subclass have been isolated into an
 * instance of this class. You can either use this class (which contains the
 * default implementation), or you can subclass it and register a bean under the
 * name specified by manifest constant
 * <code>FlowNavigationHandler.NAVIGATION_STRATEGY_BEAN_NAME</code>.
 * </p>
 * 
 * @author Craig McClanahan
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowNavigationHandlerStrategy extends FlowExecutionManager {

	/**
	 * <p>
	 * Bean name under which we will find the configured instance of the
	 * {@link FlowNavigationHandlerStrategy} to be used for determining what
	 * logical actions to undertake.
	 * </p>
	 */
	public static final String BEAN_NAME = "flowNavigationHandlerStrategy";

	/**
	 * <p>
	 * Prefix on a logical outcome value that identifies a logical outcome as
	 * the identifier for a web flow that should be entered.
	 * </p>
	 */
	protected static final String WEBFLOW_PREFIX = "webflow:";

	/**
	 * Create a new flow execution manager with the specified storage strategy.
	 * @param storage the storage strategy
	 * 
	 * @see #setFlow(Flow)
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListener(FlowExecutionListener, FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListeners(Collection, FlowExecutionListenerCriteria)
	 * @see #setTransactionSynchronizer(TransactionSynchronizer)
	 */
	public FlowNavigationHandlerStrategy(FlowLocator locator) {
		super(locator);
	}

	/**
	 * <p>
	 * Return <code>true</code> if the current request is asking for the
	 * creation of a new flow. The default implementation examines the logical
	 * outcome to see if it starts with the prefix specified by
	 * <code>WebFlowNavigationStrategy.PREFIX</code>.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowLaunchRequest(FacesContext context, String fromAction, String outcome) {
		if (outcome == null) {
			return false;
		}
		return outcome.startsWith(WEBFLOW_PREFIX);
	}

	/**
	 * <p>
	 * Create a new flow execution for the current request. Proceed until a
	 * <code>ViewDescriptor</code> is returned describing the next view that
	 * should be rendered.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param manager <code>FlowExecutionManager</code> used to manage this
	 * flow
	 */
	public ViewDescriptor launchFlowExecution(FacesContext context, String fromAction, String outcome) {
		// strip off the webflow prefix, leaving the flowId to launch
		String flowId = outcome.substring(WEBFLOW_PREFIX.length());
		Assert.hasText(flowId, "The id of the flow to launch was not provided in the outcome string "
				+ "- programmer error");
		FlowExecution flowExecution = createFlowExecution(getFlowLocator().getFlow(flowId));
		FlowExecutionHolder.setFlowExecution(null, flowExecution);
		Event event = createEvent(context, fromAction, outcome, null);
		ViewDescriptor selectedView = flowExecution.start(event);
		
		// enable following after flow execution storage changes are done
		// NOTE: the sequence of preparing the view, and then saving the storage 
		// (via the phase listener) is the reverse of that used for other web
		// frameworks, but it is needed since the JSF render phase may create
		// data in the flow scope 
		// it _does_ imply that client side flow storage may not be used, as that
		// flow storage changes the flow ID
		//return prepareSelectedView(selectedView, null, flowExecution);
		
		return afterEvent(event, null, flowExecution, selectedView);
	}

	/**
	 * Creates a flow execution listener to attach to a managed FlowExecution.
	 * @param context the faces context
	 * @return the listener to attach
	 */
	protected FlowExecutionListener createFlowExecutionListener(FacesContext context) {
		return new JsfFlowExecutionListener(context);
	}
	
	/**
	 * <p>
	 * Return <code>true</code> if there is an existing flow execution in
	 * progress for the current request. Implementors must ensure that the
	 * algorithm used to makes this determination matches the determination that
	 * will be made by the <code>FlowExecutionManager</code> that is being
	 * used. The default implementation looks for a request parameter named by
	 * <code>WebFlowNavigationStrategy.FLOW_EXECUTION_ID</code>.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowExecutionParticipationRequest(FacesContext context, String fromAction, String outcome) {
		boolean executionBound = FlowExecutionHolder.getFlowExecution() == null ? false : true;
		// assert an invariant, just to be safe
		boolean requestKeyPresent = context.getExternalContext().getRequestParameterMap().containsKey(
				FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER);
		Assert.isTrue(executionBound == requestKeyPresent,
				"FlowExecution bound to thread context must match the existence of a "
						+ "_flowExecutionId request attribute");
		return executionBound;
	}

	/**
	 * <p>
	 * Resume an active flow execution for the current request. Proceed until a
	 * <code>ViewDescriptor</code> is returned describing the next view that
	 * should be rendered.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param flowExecutionManager <code>FlowExecutionManager</code> used to
	 * manage this flow
	 */
	public ViewDescriptor resumeFlowExecution(FacesContext context, String fromAction, String outcome) {
		Serializable id = FlowExecutionHolder.getFlowExecutionId();
		FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
		Event event = createEvent(context, fromAction, outcome, null);
		ViewDescriptor selectedView = signalEventIn(flowExecution, event);
		
		// enable following after flow execution storage changes are done
		// NOTE: the sequence of preparing the view, and then saving the storage 
		// (via the phase listener) is the reverse of that used for other web
		// frameworks, but it is needed since the JSF render phase may create
		// data in the flow scope 
		// it _does_ imply that client side flow storage may not be used, as that
		// flow storage changes the flow ID
		//return prepareSelectedView(selectedView, id, flowExecution);
		
		return afterEvent(event, id, flowExecution, selectedView);
	}

	/**
	 * <p>
	 * Construct and return an <code>Event</code> reflecting the flow
	 * execution event that represents this request. The default implementation
	 * constructs and returns a <code>ServletEvent</code> reflecting the
	 * current request. FIXME - this will need to be refactored to support
	 * portlet events as well as servlet events -- or perhaps a FacesEvent can
	 * be constructed that hides the environmental differences.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param parameters Optional additional event parameters
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public Event createEvent(FacesContext context, String fromAction, String outcome, Map parameters) {
		return new JsfEvent(outcome, context, fromAction, parameters);
	}

	/**
	 * <p>
	 * Render the view specified by this <code>ViewDescriptor</code>, after
	 * exposing any model data it includes.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param manager <code>FlowExecutionManager</code> used to manage this
	 * flow
	 * @param viewDescriptor <code>ViewDescriptor</code> for the view to
	 * render
	 */
	public void renderView(FacesContext context, String fromAction, String outcome, ViewDescriptor viewDescriptor) {
		// Expose model data specified in the descriptor
		try {
			context.getExternalContext().getRequestMap().putAll(viewDescriptor.getModel());
		}
		catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll remove after it's fixed in MyFaces
			Map requestMap = context.getExternalContext().getRequestMap();
			Iterator it = viewDescriptor.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				requestMap.put(entry.getKey(), entry.getValue());
			}
		}
		// Stay on the same view if requested
		if (viewDescriptor.getViewName() == null) {
			return;
		}
		// Create the specified view so that it can be rendered
		ViewHandler vh = context.getApplication().getViewHandler();
		UIViewRoot view = vh.createView(context, viewDescriptor.getViewName());
		context.setViewRoot(view);
	}
}