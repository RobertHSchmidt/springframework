/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.execution.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.ExternalContext;

/**
 * Provides contextual information about an HTTP Servlet environment that 
 * has interacted with SWF.
 * 
 * @author Keith Donald
 */
public class ServletExternalContext implements ExternalContext {

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	/**
	 * Create a new external context wrapping given servlet HTTP request
	 * and response.
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	public ServletExternalContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	public Map getRequestParameterMap() {
		return new HttpRequestParameterMap(request);
	}

	public Map getRequestMap() {
		return new HttpRequestMap(request);
	}

	public Map getSessionMap() {
		return new HttpSessionMap(request);
	}

	public Map getApplicationMap() {
		return new HttpServletContextMap(request.getSession().getServletContext());
	}

	/**
	 * Return the wrapped HTTP servlet request.
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * Return the wrapped HTTP servlet response.
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
}