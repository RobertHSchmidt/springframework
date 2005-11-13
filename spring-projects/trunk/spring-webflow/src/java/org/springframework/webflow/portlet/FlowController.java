/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.util.Assert;
import org.springframework.web.portlet.mvc.AbstractController;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.webflow.FlowLocator;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.portlet.PortletEvent;

/**
 * Point of integration between Spring Portlet MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming portlet requests to one or more
 * managed web flows.
 * <p>
 * Requests into the web flow system are handled by a
 * {@link FlowExecutionManager}, which this class delegates to. Consult the
 * JavaDoc of that class for more information on how requests are processed.
 * <p>
 * Note that a single FlowController may manage executions for all flows of your
 * application: simply parameterize this controller from client code by
 * providing a request parameter <code>_flowId</code> indicating the flow
 * definition to execute. See the flowLauncher sample application for an example
 * of this.
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	private static final String VIEW_SELECTION_ATTRIBUTE_NAME = FlowController.class + ".viewSelection";

	/**
	 * The manager for flow executions.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * Creates a new FlowController that initially relies on a default
	 * {@link org.springframework.webflow.execution.FlowExecutionManager}
	 * implementation that uses the provided flow locator to access flow
	 * definitions at runtime.
	 */
	public FlowController(FlowLocator flowLocator) {
		initDefaults();
		setFlowExecutionManager(new FlowExecutionManager(flowLocator));
	}

	/**
	 * Create a new FlowController that delegates to the configured execution
	 * manager for managing the execution of web flows.
	 * @param flowExecutionManager the manager to launch and resume flow
	 * executions brokered by this web controller.
	 */
	public FlowController(FlowExecutionManager flowExecutionManager) {
		initDefaults();
		setFlowExecutionManager(flowExecutionManager);
	}

	/**
	 * Set default properties for this controller. * The "cacheSeconds" property
	 * is by default set to 0 (so by default there is no HTTP header caching for
	 * web flow controllers).
	 */
	protected void initDefaults() {
		// no caching
		setCacheSeconds(0);
	}

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the HTTP flow execution manager
	 */
	protected FlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 * @param manager the flow execution manager
	 */
	public void setFlowExecutionManager(FlowExecutionManager manager) {
		Assert.notNull(manager, "The flow execution manager to dispatch requests to is required");
		this.flowExecutionManager = manager;
	}

	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		// delegate to the flow execution manager to process the request
		ViewSelection selectedView = getFlowExecutionManager().onEvent(new PortletEvent(request, response));
		// expose selected view in session for access during render phase
		request.getPortletSession().setAttribute(VIEW_SELECTION_ATTRIBUTE_NAME, selectedView);
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		ViewSelection selectedView = (ViewSelection)request.getPortletSession().getAttribute(
				VIEW_SELECTION_ATTRIBUTE_NAME);
		// convert selected view to a renderable portlet mvc "model and view"
		return toModelAndView(selectedView);
	}

	/**
	 * Create a ModelAndView object based on the information in the selected
	 * view descriptor. Subclasses can override this to return a specialized
	 * ModelAndView or to do custom processing on it.
	 * @param selectedView the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewSelection selectedView) {
		if (selectedView == null) {
			return null;
		}
		String viewName = selectedView.getViewName();
		if (selectedView.isRedirect()) {
			viewName = UrlBasedViewResolver.REDIRECT_URL_PREFIX + viewName;
		}
		return new ModelAndView(viewName, selectedView.getModel());
	}
}