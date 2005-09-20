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

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.webflow.Event;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * <p>
 * Strategy methods for {@link WebFlowNavigationHandler}. Because a JSF
 * <code>NavigationHandler</code> must be registered directly with the JSF
 * runtime, the implementation class cannot be customized in the typical fashion
 * for a Spring-based application. Therefore, decisions that would typically be
 * placed in hook methods for a specialzed subclass have been isolated into an
 * instance of this class. You can either use this class (which contains the
 * default implementation), or you can subclass it and register a bean under the
 * name specified by manifest constant
 * <code>WebFlowNavigationHandler.STRATEGY_BEAN</code>.
 * </p>
 * 
 * @author Craig McClanahan
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class WebFlowNavigationStrategy {

	/**
	 * <p>
	 * Prefix on a logical outcome value that identifies a logical outcome as
	 * the identifier for a web flow that should be entered.
	 * </p>
	 */
	protected static final String WEBFLOW_PREFIX = "webflow:";

	/**
	 * The Spring webflow execution manager.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * Create a web flow navigation strategy that delegates to the flow
	 * execution manager
	 * @param flowExecutionManager the execution manager
	 */
	public WebFlowNavigationStrategy(FlowExecutionManager flowExecutionManager) {
		this.flowExecutionManager = flowExecutionManager;
	}

	/**
	 * <p>
	 * Return <code>true</code> if the current request is asking for the
	 * creation of a new flow. The default implementation examines the logical
	 * outcome to see if it starts with the prefix specified by
	 * <code>WebFlowNavigationStrategy.PREFIX</code>.
	 * </p>
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
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param manager <code>FlowExecutionManager</code> used to manage this
	 * flow
	 */
	public ViewDescriptor launchFlowExecution(FacesContext context, String fromAction, String outcome) {
		Map parameters = new HashMap(1);
		// strip of the webflow prefix, leaving the flowId to launch
		String flowId = outcome.substring(WEBFLOW_PREFIX.length());
		parameters.put(FlowExecutionManager.FLOW_ID_PARAMETER, flowId);
		return flowExecutionManager.onEvent(createEvent(context, fromAction, outcome, parameters));
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
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowExecutionParticipationRequest(FacesContext context, String fromAction, String outcome) {
		return context.getExternalContext().getRequestParameterMap().containsKey(
				FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER);
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
		return flowExecutionManager.onEvent(createEvent(context, fromAction, outcome, null));
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
		context.getExternalContext().getRequestMap().putAll(viewDescriptor.getModel());
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