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
package org.springframework.webflow.context.portlet;

import java.util.Map;
import java.util.TreeMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.ExternalContext;

/**
 * Provides contextual information about a portlet environment that has
 * interacted with SWF.
 * 
 * @author Keith Donald
 */
public class PortletExternalContext implements ExternalContext {

	/**
	 * The context.
	 */
	private PortletContext context;

	/**
	 * The request.
	 */
	private PortletRequest request;

	/**
	 * The response.
	 */
	private PortletResponse response;

	/**
	 * Create an external context wrapping given portlet request and response.
	 * @param request the portlet request
	 * @param response the portlet response
	 */
	public PortletExternalContext(PortletContext context, PortletRequest request, PortletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
	}

	public Map getRequestParameterMap() {
		return new PortletRequestParameterMap(request);
	}

	public Map getRequestMap() {
		return new PortletRequestMap(request);
	}

	public SharedMap getSessionMap() {
		return new PortletSessionMap(request);
	}

	public SharedMap getApplicationMap() {
		return new PortletContextMap(context);
	}

	/**
	 * Returns the wrapped portlet context.
	 */
	public PortletContext getContext() {
		return context;
	}

	/**
	 * Returns the wrapped portlet request.
	 */
	public PortletRequest getRequest() {
		return request;
	}

	/**
	 * Returns the wrapped portlet response.
	 */
	public PortletResponse getResponse() {
		return response;
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", new TreeMap(getRequestParameterMap()))
				.toString();
	}
}