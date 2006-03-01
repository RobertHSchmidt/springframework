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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.FlowRedirect;

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
 * {@link FlowExecutorParameterExtractor#getFlowIdParameterName()} <i>render
 * request</i> parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutorParameterExtractor#getFlowExecutionKeyParameterName()} and
 * {@link FlowExecutorParameterExtractor#getEventIdParameterName()} <i>action
 * request</i> identifying the conversation to participate in.
 * </ul>
 * <p>
 * Usage example:
 * 
 * <pre>
 *                        &lt;!--
 *                            Exposes flows for execution.
 *                        --&gt;
 *                        &lt;bean id=&quot;flowController&quot; class=&quot;org.springframework.webflow.executor.mvc.PortletFlowController&quot;&gt;
 *                            &lt;constructor-arg ref=&quot;flowRegistry&quot;/&gt;
 *                            &lt;property name=&quot;defaultFlowId&quot; value=&quot;example-flow&quot;/&gt;
 *                        &lt;/bean&gt;
 *                                                                             
 *                        &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *                        &lt;bean name=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *                            &lt;property name=&quot;flowLocations&quot; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt;
 *                        &lt;/bean&gt;
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
		parameterExtractor.setDefaultFlowId(defaultFlowId);
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		Serializable conversationId = parameterExtractor.extractConversationId(context);
		if (conversationId != null) {
			ResponseInstruction responseInstruction = getCachedResponseInstruction(request,
					getConversationAttributeName(conversationId));
			// rely on current response instruction for active conversation
			if (responseInstruction == null) {
				responseInstruction = flowExecutor.getCurrentResponseInstruction(conversationId, context);
			}
			return toModelAndView(responseInstruction);
		}
		else {
			// launch a new flow execution
			String flowId = parameterExtractor.extractFlowId(context);
			ResponseInstruction responseInstruction = flowExecutor.launch(flowId, context);
			return toModelAndView(responseInstruction);
		}
	}

	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		String eventId = parameterExtractor.extractEventId(context);
		FlowExecutionKey flowExecutionKey = parameterExtractor.extractFlowExecutionKey(context);
		ResponseInstruction responseInstruction = flowExecutor.signalEvent(eventId, flowExecutionKey, context);
		if (responseInstruction.isApplicationView() || responseInstruction.isConversationRedirect()) {
			Serializable conversationId = flowExecutionKey.getConversationId();
			response.setRenderParameter(parameterExtractor.getConversationIdParameterName(), String
					.valueOf(conversationId));
			if (responseInstruction.isConfirmationView()) {
				// cache ending response temporarily for final forward on the
				// next render request
				cacheResponseInstruction(request, responseInstruction, conversationId);
			}
		}
		else if (responseInstruction.isFlowRedirect()) {
			// request that a new flow be launched within this portlet
			String flowId = ((FlowRedirect)responseInstruction.getViewSelection()).getFlowId();
			response.setRenderParameter(parameterExtractor.getFlowIdParameterName(), flowId);
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}

	// helpers

	private ResponseInstruction getCachedResponseInstruction(PortletRequest request, String attributeName) {
		PortletSession session = request.getPortletSession(false);
		// try and grab last conversation response selection from session
		ResponseInstruction response = null;
		if (session != null) {
			response = (ResponseInstruction)session.getAttribute(attributeName);
			if (response != null) {
				// remove it
				session.removeAttribute(attributeName);
			}
		}
		return response;
	}

	protected ModelAndView toModelAndView(ResponseInstruction response) {
		if (response.isApplicationView()) {
			// forward to a view as part of an active conversation
			ApplicationView forward = (ApplicationView)response.getViewSelection();
			Map model = new HashMap(forward.getModel());
			parameterExtractor.put(response.getFlowExecutionKey(), model);
			parameterExtractor.put(response.getFlowExecutionContext(), model);
			return new ModelAndView(forward.getViewName(), model);
		}
		else if (response.isNull()) {
			return null;
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}

	private void cacheResponseInstruction(PortletRequest request, ResponseInstruction response,
			Serializable conversationId) {
		PortletSession session = request.getPortletSession(false);
		if (session != null) {
			session.setAttribute(getConversationAttributeName(conversationId), response);
		}
	}

	private String getConversationAttributeName(Serializable conversationId) {
		return "responseInstruction." + conversationId;
	}
}