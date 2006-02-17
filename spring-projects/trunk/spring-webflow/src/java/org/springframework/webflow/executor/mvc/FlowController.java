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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;
import org.springframework.webflow.executor.support.FlowRequestHandler;

/**
 * Point of integration between Spring MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming requests to one or more managed flow
 * executions.
 * <p>
 * Requests into the web flow system are handled by a {@link FlowExecutor},
 * which this class delegates to using a {@link FlowRequestHandler}. Consult
 * the JavaDoc of that class for more information on how requests are processed.
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
 *     &lt;!--
 *         Exposes flows for execution at a single request URL.
 *         The id of a flow to launch should be passed in by clients using
 *         the &quot;_flowId&quot; request parameter:
 *         e.g. /app.htm?_flowId=flow1
 *     --&gt;
 *     &lt;bean name=&quot;/app.htm&quot; class=&quot;org.springframework.webflow.executor.mvc.FlowController&quot;&gt;
 *         &lt;constructor-arg ref=&quot;flowLocator&quot;/&gt;
 *     &lt;/bean&gt;
 *                                                         
 *     &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *     &lt;bean name=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowLocations&quot; value="/WEB-INF/flows/*-flow.xml"/&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * It is also possible to customize the {@link FlowExecutorParameterExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	/**
	 * Delegate for managing flow executions (launching new executions, and
	 * resuming existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extracting flow executor parameters from a request made by a
	 * {@link ExternalContext}.
	 */
	private FlowExecutorParameterExtractor parameterExtractor = new FlowExecutorParameterExtractor();

	/**
	 * Create a new FlowController that delegates to the configured executor for
	 * driving the execution of web flows.
	 * @param flowExecutor the service to launch and resume flow executions
	 * brokered by this web controller.
	 */
	public FlowController(FlowExecutor flowExecutor) {
		initDefaults();
		setFlowExecutor(flowExecutor);
	}

	/**
	 * Convenience constructor that creates a new FlowController that initially
	 * relies on a default
	 * {@link org.springframework.webflow.executor.FlowExecutorImpl}
	 * implementation that uses the provided flow locator to access flow
	 * definitions at runtime.
	 */
	public FlowController(FlowLocator flowLocator) {
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
	 * Sets the flow executor to use.
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
		parameterExtractor.setDefaultFlowId(defaultFlowId);
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ServletExternalContext context = new ServletExternalContext(getServletContext(), request, response);
		ResponseInstruction responseInstruction = createFlowExecutorTemplate().handleFlowRequest(context);
		return toModelAndView(responseInstruction, context);
	}

	/**
	 * Factory method that creates a new helper for processing a request into
	 * this flow controller. The controller is a basic template encapsulating
	 * reusable flow execution request handling workflow.
	 * @return the controller helper
	 */
	protected FlowRequestHandler createFlowExecutorTemplate() {
		return new FlowRequestHandler(getFlowExecutor(), getParameterExtractor());
	}

	/**
	 * Create a ModelAndView object based on the information in the selected
	 * response instruction. Subclasses can override this to return a specialized
	 * ModelAndView or to do custom processing on it.
	 * @param response instruction the response instruction to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ResponseInstruction response, ExternalContext context) {
		if (response.isNull()) {
			return null;
		}
		if (response.isRestart()) {
			// restart the flow by redirecting to flow launch URL
			String flowId = response.getFlowExecutionContext().getFlow().getId();
			String flowUrl = parameterExtractor.createFlowUrl(flowId, context);
			return new ModelAndView(new RedirectView(flowUrl));
		}
		if (response.getFlowExecutionContext().isActive()) {
			if (response.isRedirect()) {
				// redirect to active conversation URL
				Serializable conversationId = response.getFlowExecutionKey().getConversationId();
				String conversationUrl = parameterExtractor.createConversationUrl(conversationId, context);
				return new ModelAndView(new RedirectView(conversationUrl, true));
			}
			else {
				// forward to a view as part of an active conversation
				Map model = new HashMap(response.getModel().size() + 2, 1);
				model.putAll(response.getModel());
				FlowExecutionKey flowExecutionKey = response.getFlowExecutionKey();
				FlowExecutionContext flowExecutionContext = response.getFlowExecutionContext();
				parameterExtractor.putContextAttributes(flowExecutionKey, flowExecutionContext, model);
				return new ModelAndView(response.getViewName(), model);
			}
		}
		else {
			if (response.isRedirect()) {
				// redirect to an external URL after flow completion
				boolean contextRelative = isContextRelativeUrl(response.getViewName());
				return new ModelAndView(new RedirectView(response.getViewName(), contextRelative), response.getModel());
			}
			else {
				// forward to a view after flow completion
				return new ModelAndView(response.getViewName(), response.getModel());
			}
		}
	}

	protected boolean isContextRelativeUrl(String viewName) {
		return viewName.startsWith("/");
	}
}