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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
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
 * A custom MVC HandlerAdapter that encapsulates the generic workflow associated with executing flows. Delegates to
 * mapped {@link FlowHandler flow handlers} to manage the interaction with executions of specific flow definitions.
 * 
 * @author Keith Donald
 */
public class FlowHandlerAdapter extends WebApplicationObjectSupport implements HandlerAdapter {

	private static final Log logger = LogFactory.getLog(FlowHandlerAdapter.class);

	/**
	 * The entry point into Spring Web Flow.
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
	 * Creates a new flow handler adapter
	 * @param flowExecutor the flow executor
	 */
	public FlowHandlerAdapter(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
		this.urlHandler = new DefaultFlowUrlHandler();
		this.ajaxHandler = new SpringJavascriptAjaxHandler();
	}

	/**
	 * Returns the flow url handler.
	 */
	public FlowUrlHandler getFlowUrlHandler() {
		return urlHandler;
	}

	/**
	 * Sets the flow url handler
	 * @param urlHandler the flow url handler
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

	public boolean supports(Object handler) {
		return handler instanceof FlowHandler;
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		FlowHandler flowHandler = (FlowHandler) handler;
		String flowExecutionKey = urlHandler.getFlowExecutionKey(request);
		if (flowExecutionKey != null) {
			try {
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.resumeExecution(flowExecutionKey, context);
				return handleFlowExecutionResult(result, context, request, response, flowHandler);
			} catch (FlowException e) {
				return handleFlowException(e, request, response, flowHandler);
			}
		} else {
			try {
				String flowId = getFlowId(flowHandler, request);
				MutableAttributeMap input = getInputMap(flowHandler, request);
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.launchExecution(flowId, input, context);
				return handleFlowExecutionResult(result, context, request, response, flowHandler);
			} catch (FlowException e) {
				return handleFlowException(e, request, response, flowHandler);
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
				if (logger.isDebugEnabled()) {
					logger.debug("Restarting a new execution of previously expired/ended flow '" + flowId + "'");
				}
				// by default, attempt to restart the flow
				response.sendRedirect(urlHandler.createFlowDefinitionUrl(flowId, null, request));
			}
			return null;
		} else {
			throw e;
		}
	}

	// internal helpers

	private ModelAndView handleFlowExecutionResult(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response, FlowHandler handler) throws IOException {
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
				String location = handler.handleExecutionOutcome(result.getEndedOutcome(), result.getEndedOutput(),
						request, response);
				return location != null ? createRedirectView(location, request) : defaultHandleFlowOutcome(result
						.getFlowId(), result.getEndedOutcome(), result.getEndedOutput(), request, response);
			}
		} else {
			throw new IllegalStateException("Execution result should have been one of [paused] or [ended]");
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

	private void sendRedirect(ServletExternalContext context, HttpServletRequest request, HttpServletResponse response,
			String targetUrl) throws IOException {
		if (context.isAjaxRequest()) {
			ajaxHandler.sendAjaxRedirect(getServletContext(), request, response, targetUrl, context.isAjaxRequest());
		} else if (!response.isCommitted()) {
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		}
	}

	private ModelAndView handleFlowException(FlowException e, HttpServletRequest request, HttpServletResponse response,
			FlowHandler handler) throws IOException {
		String location = handler.handleException(e, request, response);
		return location != null ? createRedirectView(location, request) : defaultHandleFlowException(getFlowId(handler,
				request), e, request, response);
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	private String getFlowId(FlowHandler handler, HttpServletRequest request) {
		String flowId = handler.getFlowId();
		if (flowId != null) {
			return flowId;
		} else {
			return urlHandler.getFlowId(request);
		}
	}

	private MutableAttributeMap getInputMap(FlowHandler handler, HttpServletRequest request) {
		MutableAttributeMap input = handler.createExecutionInputMap(request);
		if (input != null) {
			return input;
		} else {
			return defaultFlowExecutionInputMap(request);
		}
	}
}
