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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.webflow.Event;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.servlet.ServletEvent;

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
 * @since 1.0
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 */
public class WebFlowNavigationStrategy {

	// -------------------------------------------------------- Statoc Variables

	/**
     * <p>
     * The request parameter used to represent a flow execution id for an
     * existing in-progress flow.
     * </p>
     */
	protected static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	/**
     * <p>
     * Prefix on a logical outcome value that identifies a logical outcome as
     * the identifier for a web flow that should be entered.
     * </p>
     */
	protected static final String PREFIX					  = "webflow:";

	// -------------------------------------------------------------- Properties

	// ---------------------------------------------------------- Public Methods

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
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public boolean active(FacesContext context, String fromAction, String outcome) {

		return context.getExternalContext().getRequestParameterMap().containsKey(
				FLOW_EXECUTION_ID_PARAMETER);

	}

	/**
     * <p>
     * Create a new flow execution for the current request. Proceed until a
     * <code>ViewDescriptor</code> is returned describing the next view that
     * should be rendered.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param manager
     *            <code>FlowExecutionManager</code> used to manage this flow
     */
	public ViewDescriptor create(FacesContext context, String fromAction, String outcome,
			FlowExecutionManager manager) throws Exception {

		String flowId = outcome.substring(PREFIX.length());
		Map map = new HashMap();
		map.put(FlowExecutionManager.FLOW_ID_PARAMETER, flowId);
		return manager.onEvent(event(context, map, fromAction, outcome));

	}

	/**
     * <p>
     * Return <code>true</code> if the current request is asking for the
     * creation of a new flow. The default implementation examines the logical
     * outcome to see if it starts with the prefix specified by
     * <code>WebFlowNavigationStrategy.PREFIX</code>.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public boolean creating(FacesContext context, String fromAction, String outcome) {

		if (outcome == null) {
			return false;
		}
		return outcome.startsWith(PREFIX);

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
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param parameters
     *            Optional additional event parameters
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public Event event(FacesContext context, Map parameters, String fromAction, String outcome) {

		HttpServletRequest request = (HttpServletRequest) context.getExternalContext()
				.getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext()
				.getResponse();
		return new ServletEvent(request, response, parameters);

	}

	/**
     * <p>
     * Render the view specified by this <code>ViewDescriptor</code>, after
     * exposing any model data it includes.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param manager
     *            <code>FlowExecutionManager</code> used to manage this flow
     * @param descriptor
     *            <code>ViewDescriptor</code> for the view to render
     */
	public void render(FacesContext context, String fromAction, String outcome,
			FlowExecutionManager manager, ViewDescriptor descriptor) {

		// Assume that the view name in the descriptor corresponds to
		// a JSF view identifier
		String viewId = descriptor.getViewName();
		
		// Expose model data specified in the descriptor
		context.getExternalContext().getRequestMap().putAll(descriptor.getModel());

		// Stay on the same view if requested
		if (viewId == null) {
			return;
		}

		// Create the specified view so that it can be rendered
		ViewHandler vh = context.getApplication().getViewHandler();
		UIViewRoot view = vh.createView(context, viewId);
		context.setViewRoot(view);

	}

	/**
     * <p>
     * Resume an active flow execution for the current request. Proceed until a
     * <code>ViewDescriptor</code> is returned describing the next view that
     * should be rendered.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param manager
     *            <code>FlowExecutionManager</code> used to manage this flow
     */
	public ViewDescriptor resume(FacesContext context, String fromAction, String outcome,
			FlowExecutionManager manager) throws Exception {

		return manager.onEvent(event(context, null, fromAction, outcome));
	}

}
