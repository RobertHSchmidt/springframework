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

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.style.ToStringCreator;
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

	private FlowExecutor flowExecutor;

	private boolean flowExecutionRedirect;

	private String resourceUri;

	private FlowException exception;

	/**
	 * Create a new external context wrapping given servlet HTTP request and response and given servlet context.
	 * @param context the servlet context
	 * @param request the servlet request
	 * @param response the servlet response
	 */
	public ServletExternalContext(ServletContext context, ServletRequest request, ServletResponse response,
			FlowExecutor flowExecutor) {
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
		this.flowExecutor = flowExecutor;
		parseRequestPathInfo();
	}

	public String getFlowId() {
		return flowId;
	}

	public String getFlowExecutionKey() {
		return flowExecutionKey;
	}

	private void parseRequestPathInfo() {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			throw new IllegalArgumentException(
					"The request path is null - unable to determine flow id or flow execution key");
		}
		String[] pathElements = pathInfo.substring(1, pathInfo.length()).split("/");
		flowId = pathElements[0];
		if (pathElements.length == 1) {
			flowExecutionKey = null;
			requestElements = new String[0];
		} else if (pathElements.length > 1) {
			if (pathElements[1].equals("execution")) {
				flowExecutionKey = pathElements[2];
				requestElements = new String[0];
			} else {
				flowExecutionKey = null;
				requestElements = new String[pathElements.length - 1];
				System.arraycopy(pathElements, 1, requestElements, 0, requestElements.length);
			}
		}
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

	public void sendFlowExecutionRedirect() {
		this.flowExecutionRedirect = true;
	}

	public void sendFlowDefinitionRedirect(String flowId, String[] requestElements, ParameterMap requestParameters) {
	}

	public void sendExternalRedirect(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	// execution processing result setters

	public void setPausedResult(String flowExecutionKey) {
	}

	public void setEndedResult() {

	}

	public void setExceptionResult(FlowException e) {
		this.exception = e;
	}

	public void processRequest() {
		ExternalContextHolder.setExternalContext(this);
		try {
			flowExecutor.execute(this);
		} finally {
			ExternalContextHolder.setExternalContext(null);
		}
	}

	private void issueFlowExecutionRedirect() {
		try {
			response.sendRedirect(getFlowId().toString() + "/" + flowExecutionKey.toString());
		} catch (IOException e) {

		}
	}

	private boolean isPausedResult() {
		return flowExecutionKey != null;
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", getRequestParameterMap()).toString();
	}

}