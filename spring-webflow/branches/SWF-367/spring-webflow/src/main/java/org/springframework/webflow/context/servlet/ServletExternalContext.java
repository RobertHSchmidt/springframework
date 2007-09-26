/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.context.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.core.collection.LocalSharedAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * Provides contextual information about an HTTP Servlet environment that has interacted with Spring Web Flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ServletExternalContext implements ExternalContext {

	/** The default encoding scheme: UTF-8 */
	private static final String DEFAULT_ENCODING_SCHEME = "UTF-8";

	/**
	 * The context.
	 */
	private ServletContext context;

	/**
	 * The request.
	 */
	private HttpServletRequest request;

	/**
	 * The response.
	 */
	private HttpServletResponse response;

	/**
	 * An accessor for the HTTP request parameter map.
	 */
	private ParameterMap requestParameterMap;

	/**
	 * An accessor for the HTTP request attribute map.
	 */
	private MutableAttributeMap requestMap;

	/**
	 * An accessor for the HTTP session map.
	 */
	private SharedAttributeMap sessionMap;

	/**
	 * An accessor for the servlet context application map.
	 */
	private SharedAttributeMap applicationMap;

	private String flowId;

	private String flowExecutionKey;

	private String[] requestElements;

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;

	private boolean flowExecutionRedirect;

	private FlowDefinitionRedirector flowDefinitionRedirector;

	private String resourceUri;

	private String pausedFlowExecutionKey;

	private FlowException exception;

	/**
	 * Create a new external context wrapping given servlet HTTP request and response and given servlet context.
	 * @param context the servlet context
	 * @param request the servlet request
	 * @param response the servlet response
	 */
	public ServletExternalContext(ServletContext context, ServletRequest request, ServletResponse response) {
		this.context = context;
		try {
			this.request = (HttpServletRequest) request;
			this.response = (HttpServletResponse) response;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Request and response objects must be HTTP objects", e);
		}
		this.requestParameterMap = new LocalParameterMap(new HttpServletRequestParameterMap(this.request));
		this.requestMap = new LocalAttributeMap(new HttpServletRequestMap(this.request));
		this.sessionMap = new LocalSharedAttributeMap(new HttpSessionMap(this.request));
		this.applicationMap = new LocalSharedAttributeMap(new HttpServletContextMap(context));
		parseRequestPathInfo();
	}

	public String getFlowId() {
		return flowId;
	}

	public String getFlowExecutionKey() {
		return flowExecutionKey;
	}

	public String getRequestMethod() {
		return request.getMethod();
	}

	public String[] getRequestElements() {
		return requestElements;
	}

	public ParameterMap getRequestParameterMap() {
		return requestParameterMap;
	}

	public MutableAttributeMap getRequestMap() {
		return requestMap;
	}

	public SharedAttributeMap getSessionMap() {
		return sessionMap;
	}

	public SharedAttributeMap getGlobalSessionMap() {
		return getSessionMap();
	}

	public SharedAttributeMap getApplicationMap() {
		return applicationMap;
	}

	public Object getContext() {
		return context;
	}

	public Object getRequest() {
		return request;
	}

	public Object getResponse() {
		return response;
	}

	// response requesters

	// TODO fragment support?
	public void sendFlowExecutionRedirect() {
		this.flowExecutionRedirect = true;
	}

	// TODO fragment support?
	public void sendFlowDefinitionRedirect(String flowId, String[] requestElements, ParameterMap requestParameters) {
		flowDefinitionRedirector = new FlowDefinitionRedirector(flowId, requestElements, requestParameters);
	}

	public void sendExternalRedirect(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	// execution processing result setters

	public void setPausedResult(String flowExecutionKey) {
		this.pausedFlowExecutionKey = flowExecutionKey;
	}

	public void setEndedResult() {

	}

	public void setExceptionResult(FlowException e) {
		this.exception = e;
	}

	public void execute(FlowExecutor flowExecutor) throws IOException {
		ExternalContextHolder.setExternalContext(this);
		try {
			flowExecutor.execute(this);
			if (isPausedResult()) {
				if (flowExecutionRedirect) {
					issueFlowExecutionRedirect();
					return;
				} else if (flowDefinitionRedirector != null) {
					flowDefinitionRedirector.issueRedirect(request, response, encodingScheme);
				} else if (resourceUri != null) {
					sendRedirect(resourceUri);
				} else {
					// commit response?
				}
			} else if (isEndResult()) {
				if (flowExecutionRedirect) {
					throw new IllegalStateException(
							"You cannot send a flow execution redirect when the execution has ended - programmer error");
				} else if (flowDefinitionRedirector != null) {
					flowDefinitionRedirector.issueRedirect(request, response, encodingScheme);
				} else if (resourceUri != null) {
					sendRedirect(resourceUri);
				} else {
					// commit response?
				}
			} else if (isExceptionResult()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
			}
		} finally {
			ExternalContextHolder.setExternalContext(null);
		}
	}

	private void parseRequestPathInfo() {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			throw new IllegalArgumentException(
					"The request path is null - unable to determine flow id or flow execution key");
		}
		String[] pathElements = pathInfo.substring(1, pathInfo.length()).split("/");
		if (pathElements[0].equals("executions")) {
			flowId = pathElements[1];
			flowExecutionKey = pathElements[2];
			requestElements = null;
		} else {
			flowId = pathElements[0];
			if (pathElements.length > 1) {
				requestElements = new String[pathElements.length - 1];
				System.arraycopy(pathElements, 1, requestElements, 0, requestElements.length);
			} else {
				requestElements = null;
			}
		}
	}

	private void issueFlowExecutionRedirect() throws IOException {
		sendRedirect(request.getContextPath() + "/executions/" + getFlowId() + "/" + pausedFlowExecutionKey.toString());
	}

	private boolean isPausedResult() {
		return pausedFlowExecutionKey != null;
	}

	private boolean isEndResult() {
		return !isPausedResult() && !isExceptionResult();
	}

	private boolean isExceptionResult() {
		return exception != null;
	}

	private void sendRedirect(String targetUrl) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(targetUrl));
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", getRequestParameterMap()).toString();
	}

	private static class FlowDefinitionRedirector {
		private String flowId;
		private String[] requestElements;
		private ParameterMap requestParameters;

		public FlowDefinitionRedirector(String flowId, String[] requestElements, ParameterMap requestParameters) {
			Assert.hasText(flowId, "The id of the flow definition to redirect to is required");
			this.flowId = flowId;
			this.requestElements = requestElements;
			this.requestParameters = requestParameters;
		}

		public void issueRedirect(HttpServletRequest request, HttpServletResponse response, String encodingScheme)
				throws IOException {
			String targetUrl = request.getContextPath() + "/" + flowId + requestElements(encodingScheme)
					+ requestParameters(encodingScheme);
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		}

		private String requestElements(String encodingScheme) throws UnsupportedEncodingException {
			if (requestElements == null || requestElements.length == 0) {
				return "";
			} else {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < requestElements.length; i++) {
					buffer.append('/').append(encode(requestElements[i], encodingScheme));
				}
				return buffer.toString();
			}
		}

		private String requestParameters(String encodingScheme) throws UnsupportedEncodingException {
			if (requestParameters == null || requestParameters.isEmpty()) {
				return "";
			}
			StringBuffer queryString = new StringBuffer();
			queryString.append('?');
			Iterator it = requestParameters.asMap().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String parameterName = encode((String) entry.getKey(), encodingScheme);
				String parameterValue = encode((String) entry.getValue(), encodingScheme);
				queryString.append(parameterName).append('=').append(parameterValue);
				if (it.hasNext()) {
					queryString.append('&');
				}
			}
			return queryString.toString();
		}

		private String encode(String value, String encodingScheme) throws UnsupportedEncodingException {
			return URLEncoder.encode(value, encodingScheme);
		}
	}

}