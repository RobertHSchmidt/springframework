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
package org.springframework.webflow.executor.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;

/**
 * Point of integration between Spring Portlet MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming portlet requests to one or more
 * managed flow executions.
 * <p>
 * Requests into the web flow system are handled by a {@link FlowExecutor},
 * which this class delegates to. Consult the JavaDoc of that class for more
 * information on how requests are processed.
 * <p>
 * Note: a single FlowController may execute all flows of your application.
 * Specifically:
 * <ul>
 * <li>To have this controller launch a new flow execution (conversation), have
 * the client send a
 * {@link FlowExecutorParameterExtractor#getFlowIdParameterName()} request
 * parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutorParameterExtractor#getFlowExecutionIdParameterName()}
 * request parameter identifying the conversation to participate in.
 * </ul>
 * <p>
 * See the flowLauncher sample application for an example of this controller
 * parameterization.
 * <p>
 * Usage example:
 * 
 * <pre>
 *                   &lt;!--
 *                       Exposes flows for execution at a single request URL.
 *                       The id of a flow to launch should be passed in by clients using
 *                       the &quot;_flowId&quot; request parameter:
 *                           e.g. /app.htm?_flowId=flow1
 *                   --&gt;
 *                   &lt;bean name=&quot;/app.htm&quot; class=&quot;org.springframework.webflow.executor.mvc.PortletFlowController&quot;&gt;
 *                       &lt;constructor-arg ref=&quot;flowLocator&quot;/&gt;
 *                   &lt;/bean&gt;
 *                                              
 *                   &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *                   &lt;bean name=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *                       &lt;property name=&quot;flowLocations&quot;&gt;
 *                           &lt;list&gt;
 *                               &lt;value&gt;/WEB-INF/flow1.xml&quot;&lt;/value&gt;
 *                               &lt;value&gt;/WEB-INF/flow2.xml&quot;&lt;/value&gt;
 *                           &lt;/list&gt;
 *                       &lt;/property&gt;
 *                   &lt;/bean&gt;
 * </pre>
 * 
 * It is also possible to customize the {@link FlowExecutorParameterExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper.
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class PortletFlowController extends AbstractController {

	/**
	 * The attribute name of the last <code>ViewSelection</code> made by this
	 * controller for one user's <code>PortletSession</code>
	 */
	private static final String RESPONSE_INSTRUCTION_ATTRIBUTE_NAME = PortletFlowController.class
			+ ".responseInstruction";

	/**
	 * Delegate for executing flow executions (launching new executions, and
	 * resuming existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extracting flow executor parameters.
	 */
	private FlowExecutorParameterExtractor parameterExtractor = new FlowExecutorParameterExtractor();

	/**
	 * Create a new PortletFlowController that delegates to the configured
	 * executor for driving the execution of web flows.
	 * @param flowExecutor the service to launch and resume flow executions
	 * brokered by this web controller.
	 */
	public PortletFlowController(FlowExecutor flowExecutor) {
		initDefaults();
		setFlowExecutor(flowExecutor);
	}

	/**
	 * Convenience constructor that creates a new PortletFlowController that
	 * initially relies on a default
	 * {@link org.springframework.webflow.executor.FlowExecutorImpl}
	 * implementation that uses the provided flow locator to access flow
	 * definitions at runtime.
	 */
	public PortletFlowController(FlowLocator flowLocator) {
		initDefaults();
		setFlowExecutor(new FlowExecutorImpl(flowLocator));
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
	 * Returns the flow executor used by this controller.
	 * @return the flow executor
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Configures the flow executor implementation to use.
	 * @param flowExecutor the flow executor
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Returns the flow executor parameter extractor used by this controller.
	 * @return the parameter extractor
	 */
	public FlowExecutorParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	/**
	 * Sets the flow executor parameter extractor to use.
	 * @param parameterExtractor the parameter extractor
	 */
	public void setParameterExtractor(FlowExecutorParameterExtractor parameterExtractor) {
		this.parameterExtractor = parameterExtractor;
	}

	/**
	 * Sets the default flow to launch when this flow controller is rendered.
	 * @param defaultFlowId the id of the default flow
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		this.parameterExtractor.setDefaultFlowId(defaultFlowId);
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		ResponseInstruction responseInstruction = (ResponseInstruction)getCurrentResponse(request);
		if (responseInstruction == null) {
			PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
			responseInstruction = flowExecutor.launch(parameterExtractor.extractFlowId(context), context);
			setCurrentResponse(request, responseInstruction);
		}
		// convert view to a renderable portlet mvc "model and view"
		return toModelAndView(responseInstruction);
	}

	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		// resume a flow execution
		ResponseInstruction responseInstruction = flowExecutor.signalEvent(parameterExtractor.extractEventId(context),
				parameterExtractor.extractFlowExecutionKey(context), context);
		// expose selected view in session for access during render phase
		setCurrentResponse(request, responseInstruction);
	}

	private ResponseInstruction getCurrentResponse(PortletRequest request) {
		return (ResponseInstruction)request.getPortletSession().getAttribute(RESPONSE_INSTRUCTION_ATTRIBUTE_NAME);
	}

	private void setCurrentResponse(PortletRequest request, ResponseInstruction responseDescriptor) {
		request.getPortletSession().setAttribute(RESPONSE_INSTRUCTION_ATTRIBUTE_NAME, responseDescriptor);
	}

	protected ModelAndView toModelAndView(ResponseInstruction response) {
		if (response.isNull()) {
			return null;
		}
		if (response.getFlowExecutionContext().isActive()) {
			// forward to a view as part of an active conversation
			Map model = new HashMap(response.getModel().size() + 2, 1);
			model.putAll(response.getModel());
			FlowExecutionKey flowExecutionKey = response.getFlowExecutionKey();
			FlowExecutionContext flowExecutionContext = response.getFlowExecutionContext();
			parameterExtractor.putContextAttributes(flowExecutionKey, flowExecutionContext, model);
			return new ModelAndView(response.getViewName(), model);
		}
		else {
			// forward to a view after flow completion
			return new ModelAndView(response.getViewName(), response.getModel());
		}
	}
}