/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.mvc.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * The adapter between the Spring MVC Controller layer and the Spring Web Flow engine. This controller allows Spring Web
 * Flow to run embedded as a Controller within a DispatcherServlet, the key piece of the Spring Web MVC platform. It is
 * expected a DispatcherServlet HandlerMapping will care for mapping all requests for flows to this controller for
 * handling.
 * 
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	private static final Log logger = LogFactory.getLog(FlowController.class);

	/**
	 * The central service for executing flows and the entry point into the Web Flow system.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * A strategy for extracting flow arguments and generating flow urls.
	 */
	private FlowUrlHandler urlHandler;

	/**
	 * The representation of an Ajax client service capable of interacting with web flow.
	 */
	private AjaxHandler ajaxHandler;

	/**
	 * Specific handlers this controller should delegate to, for customizing the control logic associated with managing
	 * the execution of a specific flow.
	 */
	private Map flowHandlers = new HashMap();

	public FlowController() {
		initDefaults();
	}

	/**
	 * Creates a new flow controller.
	 * @param flowExecutor the web flow executor service
	 */
	public FlowController(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
		initDefaults();
	}

	/**
	 * Returns the central service for executing flows and the entry point into the Web Flow system.
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Sets the central service for executing flows and the entry point into the Web Flow system.
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Returns the configured flow url handler.
	 */
	public FlowUrlHandler getFlowUrlHandler() {
		return urlHandler;
	}

	/**
	 * Sets the configured flow url handler.
	 * @param urlHandler the flow url handler.
	 */
	public void setFlowUrlHandler(FlowUrlHandler urlHandler) {
		this.urlHandler = urlHandler;
	}

	/**
	 * Returns the configured Ajax handler.
	 */
	public AjaxHandler getAjaxHandler() {
		return ajaxHandler;
	}

	/**
	 * Sets the configured Ajax handler.
	 * @param ajaxHandler the ajax handler
	 */
	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		this.ajaxHandler = ajaxHandler;
	}

	/**
	 * Sets the custom flow handles for managing access to specific flows in a custom manner.
	 * @param flowHandlers the flow handler map
	 */
	public void setFlowHandlers(Map flowHandlers) {
		this.flowHandlers = flowHandlers;
	}

	/**
	 * Registers a handler for managing access to a specific flow definition.
	 * @param handler the flow handler
	 */
	public void registerFlowHandler(FlowHandler handler) {
		flowHandlers.put(handler.getFlowId(), handler);
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String flowExecutionKey = urlHandler.getFlowExecutionKey(request);
		if (flowExecutionKey != null) {
			try {
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.resumeExecution(flowExecutionKey, context);
				return handleFlowExecutionResult(result, context, request, response);
			} catch (FlowException e) {
				return handleFlowException(e, request, response);
			}
		} else {
			try {
				String flowId = urlHandler.getFlowId(request);
				MutableAttributeMap input = getFlowInput(flowId, request);
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.launchExecution(flowId, input, context);
				return handleFlowExecutionResult(result, context, request, response);
			} catch (FlowException e) {
				return handleFlowException(e, request, response);
			}
		}
	}

	// subclassing hooks

	protected ServletExternalContext createServletExternalContext(HttpServletRequest request,
			HttpServletResponse response) {
		ServletExternalContext context = new ServletExternalContext(getServletContext(), request, response, urlHandler);
		context.setAjaxRequest(ajaxHandler.isAjaxRequest(getServletContext(), request, response));
		return context;
	}

	protected MutableAttributeMap defaultFlowExecutionInputMap(HttpServletRequest request) {
		LocalAttributeMap inputMap = new LocalAttributeMap();
		Map parameterMap = request.getParameterMap();
		Iterator it = parameterMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String name = (String) entry.getKey();
			String[] values = (String[]) entry.getValue();
			if (values.length == 1) {
				inputMap.put(name, values[0]);
			} else {
				inputMap.put(name, values);
			}
		}
		return inputMap;
	}

	protected ModelAndView defaultHandleFlowOutcome(String flowId, String outcome, AttributeMap endedOutput,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!response.isCommitted()) {
			// by default, just start the flow over passing the output as input
			if (logger.isDebugEnabled()) {
				logger.debug("Restarting a new execution of ended flow '" + flowId + "'");
			}
			response.sendRedirect(urlHandler.createFlowDefinitionUrl(flowId, endedOutput, request));
		}
		return null;
	}

	protected ModelAndView defaultHandleFlowException(String flowId, FlowException e, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (e instanceof NoSuchFlowExecutionException && flowId != null) {
			if (!response.isCommitted()) {
				// by default, attempt to restart the flow
				if (logger.isDebugEnabled()) {
					logger.debug("Restarting a new execution of previously expired/ended flow '" + flowId + "'");
				}
				response.sendRedirect(urlHandler.createFlowDefinitionUrl(flowId, null, request));
			}
			return null;
		} else {
			throw e;
		}
	}

	// internal helpers

	private void initDefaults() {
		urlHandler = new DefaultFlowUrlHandler();
		ajaxHandler = new SpringJavascriptAjaxHandler();
		// set the cache seconds property to 0 so no pages are cached by default for flows
		setCacheSeconds(0);
	}

	private ModelAndView handleFlowExecutionResult(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (result.paused()) {
			if (context.flowExecutionRedirectRequested()) {
				String url = urlHandler.createFlowExecutionUrl(result.getFlowId(), result.getPausedKey(), request);
				if (logger.isDebugEnabled()) {
					logger.debug("Sending flow execution redirect to " + url);
				}
				sendRedirect(context, request, response, url);
				return null;
			} else if (context.externalRedirectRequested()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Sending external redirect to " + context.getExternalRedirectUrl());
				}
				sendRedirect(context, request, response, context.getExternalRedirectUrl());
				return null;
			} else {
				// nothing to do: flow has handled the response
				return null;
			}
		} else if (result.ended()) {
			if (context.flowDefinitionRedirectRequested()) {
				String flowId = context.getFlowRedirectFlowId();
				AttributeMap input = context.getFlowRedirectFlowInput();
				String url = urlHandler.createFlowDefinitionUrl(flowId, input, request);
				if (logger.isDebugEnabled()) {
					logger.debug("Sending flow definition to " + url);
				}
				sendRedirect(context, request, response, url);
				return null;
			} else if (context.externalRedirectRequested()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Sending external redirect to " + context.getExternalRedirectUrl());
				}
				sendRedirect(context, request, response, context.getExternalRedirectUrl());
				return null;
			} else {
				return handleFlowOutcome(result.getFlowId(), result.getEndedOutcome(), result.getEndedOutput(),
						request, response);
			}
		} else {
			throw new IllegalStateException("Execution result should have been one of [paused] or [ended]");
		}
	}

	private void sendRedirect(ServletExternalContext context, HttpServletRequest request, HttpServletResponse response,
			String targetUrl) throws IOException {
		if (context.isAjaxRequest()) {
			ajaxHandler.sendAjaxRedirect(getServletContext(), request, response, targetUrl, context.redirectInPopup());
		} else if (!response.isCommitted()) {
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		}
	}

	private MutableAttributeMap getFlowInput(String flowId, HttpServletRequest request) {
		FlowHandler handler = getFlowHandler(flowId);
		if (handler != null) {
			return handler.createExecutionInputMap(request);
		} else {
			return defaultFlowExecutionInputMap(request);
		}
	}

	private ModelAndView handleFlowOutcome(String flowId, String outcome, AttributeMap endedOutput,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		FlowHandler handler = getFlowHandler(flowId);
		if (handler != null) {
			String location = handler.handleExecutionOutcome(outcome, endedOutput, request, response);
			return location != null ? createRedirectView(location, request) : defaultHandleFlowOutcome(flowId, outcome,
					endedOutput, request, response);
		} else {
			return defaultHandleFlowOutcome(flowId, outcome, endedOutput, request, response);
		}
	}

	private ModelAndView createRedirectView(String location, HttpServletRequest request) {
		if (location.startsWith("/")) {
			return new ModelAndView(new RedirectView(location, true));
		} else {
			StringBuffer url = new StringBuffer(request.getServletPath());
			url.append('/');
			url.append(location);
			return new ModelAndView(new RedirectView(url.toString(), true));
		}
	}

	private ModelAndView handleFlowException(FlowException e, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String flowId = urlHandler.getFlowId(request);
		if (flowId != null) {
			FlowHandler handler = getFlowHandler(flowId);
			if (handler != null) {
				String location = handler.handleException(e, request, response);
				return location != null ? createRedirectView(location, request) : defaultHandleFlowException(flowId, e,
						request, response);
			} else {
				return defaultHandleFlowException(flowId, e, request, response);
			}
		} else {
			return defaultHandleFlowException(null, e, request, response);
		}
	}

	private FlowHandler getFlowHandler(String flowId) {
		return (FlowHandler) flowHandlers.get(flowId);
	}
}