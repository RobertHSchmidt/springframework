/*
 * Copyright 2002-2006 the original author or authors.
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
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
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
 * Note: a single PortletFlowController may execute all flows within your
 * application. See the phonebook-portlet sample application for examples of the
 * various strategies for launching and resuming flow executions in a Portlet
 * environment.
 * </ul>
 * <p>
 * Usage example:
 * 
 * <pre>
 * &lt;!--
 *     Exposes flows for execution.
 * --&gt;
 * &lt;bean id=&quot;flowController&quot; class=&quot;org.springframework.webflow.executor.mvc.PortletFlowController&quot;&gt;
 *     &lt;property name=&quot;flowLocator&quot; ref=&quot;flowRegistry&quot;/&gt;
 *     &lt;property name=&quot;defaultFlowId&quot; value=&quot;example-flow&quot;/&gt;
 * &lt;/bean&gt;
 *                                                                                                      
 * &lt;!-- Creates the registry of flow definitions for this application --&gt;
 * &lt;bean name=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *     &lt;property name=&quot;flowLocations&quot; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * <p>
 * It is also possible to customize the {@link FlowExecutorArgumentExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper.
 * 
 * @see org.springframework.webflow.executor.FlowExecutor
 * @see org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor
 * 
 * @author J.Enrique Ruiz
 * @author C�sar Ordi�ana
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class PortletFlowController extends AbstractController implements InitializingBean {

	/**
	 * Name of the attribute under which the response instruction
	 * will be stored in the session.
	 */
	private static final String RESPONSE_INSTRUCTION_SESSION_ATTRIBUTE = "actionRequest.responseInstruction";


	/**
	 * Delegate for executing flow executions (launching new executions, and
	 * resuming existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extracting flow executor arguments.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowExecutorArgumentExtractor();

	/**
	 * Create a new portlet flow controller. Allows for bean style usage.
	 * @see #setFlowExecutor(FlowExecutor)
	 * @see #setFlowLocator(FlowLocator)
	 */
	public PortletFlowController() {
		// set the cache seconds property to 0 so no pages are cached by default
		// for flows
		setCacheSeconds(0);
		// this controller stores ResponseInstruction objects in the session, so
		// we need to ensure we do this in an orderly manner
		// see exposeToRenderPhase() and extractActionResponseInstruction()
		setSynchronizeOnSession(true);
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
	 * @param flowExecutor the fully configured flow executor
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Sets the flow locator responsible for loading flow definitions when
	 * requested for execution by clients.
	 * <p>
	 * This is a convenience setter that configures a {@link FlowExecutorImpl}
	 * with a default {@link DefaultFlowExecutionRepositoryFactory} for managing
	 * the storage of executing flows.
	 * <p>
	 * Don't use this together with {@link #setFlowExecutor(FlowExecutor)}.
	 * @param flowLocator the locator responsible for loading flow definitions
	 * when this controller is invoked
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		flowExecutor = new FlowExecutorImpl(new DefaultFlowExecutionRepositoryFactory(flowLocator));
	}

	/**
	 * Returns the flow executor argument extractor used by this controller.
	 * @return the argument extractor
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the flow executor argument extractor to use.
	 * @param argumentExtractor the fully configured argument extractor
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	/**
	 * Sets the identifier of the default flow to launch if no flowId argument
	 * can be extracted by the configured {@link FlowExecutorArgumentExtractor}
	 * during render request processing.
	 * <p>
	 * This is a convenience method that sets the default flow id of the
	 * controller's argument extractor. Don't use this when using
	 * {@link #setArgumentExtractor(FlowExecutorArgumentExtractor)}.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		argumentExtractor.setDefaultFlowId(defaultFlowId);
	}

	public void afterPropertiesSet() {
		Assert.notNull(flowExecutor, "The flow executor property is required");
		Assert.notNull(argumentExtractor, "The argument extractor property is required");
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		if (argumentExtractor.isFlowExecutionKeyPresent(context)) {
			// flowExecutionKey render param present: this is a request to
			// render an active flow execution -- extract its key
			String flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
			// look for a cached response instruction in the session put there by
			// the action request phase as part of an "active view" forward
			ResponseInstruction responseInstruction = extractActionResponseInstruction(request);
			if (responseInstruction == null) {
				// no response instruction found, simply refresh the current
				// view state of the flow execution
				return toModelAndView(flowExecutor.refresh(flowExecutionKey, context));
			}
			else {
				// found: convert it to model and view for rendering
				return toModelAndView(responseInstruction);
			}
		}
		else {
			// this is either a "launch" flow request or a "confirmation view"
			// render request -- look for the cached "confirmation view"
			// response instruction
			ResponseInstruction responseInstruction = extractActionResponseInstruction(request);
			if (responseInstruction == null) {
				// no response instruction found in session - launch a new flow
				// execution
				String flowId = argumentExtractor.extractFlowId(context);
				return toModelAndView(flowExecutor.launch(flowId, context));
			}
			else {
				// found: convert it to model and view for rendering
				return toModelAndView(responseInstruction);
			}
		}
	}

	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		String flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
		String eventId = argumentExtractor.extractEventId(context);
		// signal the event against the flow execution, returning the next
		// response instruction
		ResponseInstruction responseInstruction = flowExecutor.signalEvent(eventId, flowExecutionKey, context);
		if (responseInstruction.isApplicationView()) {
			// response instruction is a forward to an "application view"
			if (responseInstruction.isActiveView()) {
				// is an "active" forward from a view-state (not end-state) --
				// set the flow execution key render parameter to support
				// browser refresh
				response.setRenderParameter(
						argumentExtractor.getFlowExecutionKeyParameterName(),
						responseInstruction.getFlowExecutionKey());
			}
			// cache response instruction for access during render phase of this
			// portlet
			exposeToRenderPhase(responseInstruction, request);
		}
		else if (responseInstruction.isFlowExecutionRedirect()) {
			// is a flow execution redirect: simply expose key parameter to
			// support refresh during render phase
			response.setRenderParameter(
					argumentExtractor.getFlowExecutionKeyParameterName(),
					responseInstruction.getFlowExecutionKey());
		}
		else if (responseInstruction.isFlowRedirect()) {
			// set flow id render parameter to request that a new flow be
			// launched within this portlet
			String flowId = ((FlowRedirect)responseInstruction.getViewSelection()).getFlowId();
			response.setRenderParameter(argumentExtractor.getFlowIdParameterName(), flowId);
		}
		else if (responseInstruction.isExternalRedirect()) {
			// issue the redirect to the external URL
			ExternalRedirect redirect = (ExternalRedirect)responseInstruction.getViewSelection();
			String url = argumentExtractor.createExternalUrl(redirect, flowExecutionKey, context);
			response.sendRedirect(url);
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + responseInstruction);
		}
	}

	// helpers

	/**
	 * Expose given response instruction to the render phase by
	 * putting it in the session.
	 */
	private void exposeToRenderPhase(ResponseInstruction responseInstruction, ActionRequest request) {
		PortletSession session = request.getPortletSession(false);
		if (session != null) {
			session.setAttribute(RESPONSE_INSTRUCTION_SESSION_ATTRIBUTE, responseInstruction);
		}
	}

	/**
	 * Extract a response instruction stored in the session during the
	 * action phase by {@link #exposeToRenderPhase(ResponseInstruction, ActionRequest)}.
	 * If a response instruction is found, it will be removed from the session.
	 * @param request the portlet request
	 * @return the response instructions found in the session or null if not found
	 */
	private ResponseInstruction extractActionResponseInstruction(PortletRequest request) {
		PortletSession session = request.getPortletSession(false);
		ResponseInstruction response = null;
		if (session != null) {
			response = (ResponseInstruction)session.getAttribute(RESPONSE_INSTRUCTION_SESSION_ATTRIBUTE);
			if (response != null) {
				// remove it
				session.removeAttribute(RESPONSE_INSTRUCTION_SESSION_ATTRIBUTE);
			}
		}
		return response;
	}

	/**
	 * Convert given response instruction into a Spring Portlet MVC
	 * model and view.
	 */
	protected ModelAndView toModelAndView(ResponseInstruction response) {
		if (response.isApplicationView()) {
			// forward to a view as part of an active conversation
			ApplicationView forward = (ApplicationView)response.getViewSelection();
			Map model = new HashMap(forward.getModel());
			argumentExtractor.put(response.getFlowExecutionKey(), model);
			argumentExtractor.put(response.getFlowExecutionContext(), model);
			return new ModelAndView(forward.getViewName(), model);
		}
		else if (response.isNull()) {
			return null;
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}
}