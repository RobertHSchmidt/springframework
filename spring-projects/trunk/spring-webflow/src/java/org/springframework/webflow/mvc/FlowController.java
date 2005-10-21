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
package org.springframework.webflow.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.servlet.ServletEvent;

/**
 * Web controller for the Spring web MVC framework that routes incoming requests
 * to one or more managed web flows. Requests into the web flow system are
 * managed using a configurable {@link FlowExecutionManager}. Consult the
 * JavaDoc of that class for more information on how requests are processed.
 * <p>
 * Note that a single FlowController may manage executions for all flows of your
 * application -- simply parameterize this controller from view code with the
 * <code>_flowId</code> to execute. See the flowLauncher sample application
 * for an example of this.
 * <p>
 * Configuration note: you may achieve fine-grained control over flow execution
 * management by passing in a configured flow execution manager instance.
 * Alternatively, if this controller should manage executions in the default
 * manner for a single flow definition, simply configure the flow property.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowExecutionManager</td>
 * <td>{@link org.springframework.webflow.execution.FlowExecutionManager default}</td>
 * <td>Configures the flow execution manager implementation to use.</td>
 * </tr>
 * </table>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	/**
	 * The manager for flow executions.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * Create a new FlowController.
	 * <p>
	 * The "cacheSeconds" property is by default set to 0 (so no caching for web
	 * flow controllers).
	 */
	public FlowController(FlowExecutionManager flowExecutionManager) {
		initDefaults();
		setFlowExecutionManager(flowExecutionManager);
	}

	/**
	 * Set default properties for this controller.
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

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// delegate to the flow execution manager to process the request
		ViewDescriptor selectedView = getFlowExecutionManager().onEvent(new ServletEvent(request, response));
		// convert the view descriptor to a ModelAndView object
		return toModelAndView(selectedView);
	}

	/**
	 * Create a ModelAndView object based on the information in given view
	 * descriptor. Subclasses can override this to return a specialized
	 * ModelAndView or to do custom processing on it.
	 * @param selectedView the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewDescriptor selectedView) {
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