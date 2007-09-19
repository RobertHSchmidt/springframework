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
import org.springframework.util.StringUtils;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.core.collection.LocalSharedAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.execution.FlowExecutionKey;
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

	private FlowExecutor flowExecutor;

	private FlowExecutionKey flowExecutionKey;

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
	}

	public String getContextPath() {
		return request.getContextPath();
	}

	public String getDispatcherPath() {
		return request.getServletPath();
	}

	public String getRequestPathInfo() {
		return request.getPathInfo();
	}

	public String getRequestMethod() {
		return request.getMethod();
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

	/**
	 * Return the wrapped HTTP servlet context.
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * Return the wrapped HTTP servlet request.
	 */
	public Object getRequest() {
		return request;
	}

	/**
	 * Return the wrapped HTTP servlet response.
	 */
	public Object getResponse() {
		return response;
	}

	public void sendFlowExecutionRedirect() {
		this.flowExecutionRedirect = true;
	}

	public void sendFlowDefinitionRedirect(String flowId, MutableAttributeMap input) {

	}

	public void sendExternalRedirect(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	public void setPausedResult(FlowExecutionKey key) {
		this.flowExecutionKey = key;
	}

	public void setEndedResult() {

	}

	public void setExceptionResult(FlowException e) {
		this.exception = e;
	}

	public void processRequest() {
		String executionKey = getFlowExecutionKey();
		if (executionKey != null) {
			flowExecutor.resumeExecution(executionKey, this);
		} else {
			flowExecutor.launchExecution(getFlowId(), this);
		}
		if (isPausedResult()) {
			if (flowExecutionRedirect) {
				issueFlowExecutionRedirect();
			} else {
				// flush writer?
			}
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

	private String getFlowExecutionKey() {
		String requestInfo = request.getPathInfo();
		String[] pathElements = StringUtils.tokenizeToStringArray(requestInfo, "/");
		if (pathElements.length == 2) {
			return pathElements[1];
		} else {
			return null;
		}
	}

	private FlowId getFlowId() {
		return FlowId.valueOf(StringUtils.tokenizeToStringArray(request.getPathInfo(), "/")[0]);
	}

	private AttributeMap getFlowInput() {
		return inputMapBuilder.buildInputMap(getFlowId(), this);
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", getRequestParameterMap()).toString();
	}
}