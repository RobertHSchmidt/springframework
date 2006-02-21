/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.test;

import java.util.HashMap;

import org.springframework.binding.attribute.AttributeCollection;
import org.springframework.binding.attribute.AttributeMap;
import org.springframework.binding.attribute.SharedAttributeMap;
import org.springframework.binding.attribute.UnmodifiableAttributeMap;
import org.springframework.binding.util.SharedMapDecorator;
import org.springframework.webflow.ExternalContext;

/**
 * Mock implementation of the <code>ExternalContext</code> interface.
 * 
 * @author Keith Donald
 */
public class MockExternalContext implements ExternalContext {

	private String dispatcherPath;

	private String requestPathInfo;

	private AttributeMap requestParameterMap = new AttributeMap();

	private AttributeMap requestMap = new AttributeMap();

	private SharedAttributeMap sessionMap = new SharedAttributeMap(new SharedMapDecorator(new HashMap()));

	private SharedAttributeMap applicationMap = new SharedAttributeMap(new SharedMapDecorator(new HashMap()));

	/**
	 * Creates a mock external context with an empty request parameter map.
	 */
	public MockExternalContext() {

	}

	/**
	 * Creates a mock external context with a single parameter in the request
	 * parameter map.
	 * @param singleRequestParameterName the parameter name
	 * @param singleRequestParameterValue the parameter value
	 */
	public MockExternalContext(String singleRequestParameterName, Object singleRequestParameterValue) {
		requestParameterMap = new AttributeMap(1, 1);
		requestParameterMap.setAttribute(singleRequestParameterName, singleRequestParameterValue);
	}

	/**
	 * Creates a mock external context with the specified parameters in the
	 * request parameter map.
	 * @param requestParameters the request parameters
	 */
	public MockExternalContext(AttributeCollection requestParameters) {
		requestParameterMap.addAttributes(requestParameters);
	}

	// implementing external context
	
	public String getDispatcherPath() {
		return dispatcherPath;
	}

	public String getRequestPathInfo() {
		return requestPathInfo;
	}

	public UnmodifiableAttributeMap getRequestParameterMap() {
		return requestParameterMap.unmodifiable();
	}

	public AttributeMap getRequestMap() {
		return requestMap;
	}

	public SharedAttributeMap getSessionMap() {
		return sessionMap;
	}

	public SharedAttributeMap getApplicationMap() {
		return applicationMap;
	}

	// helper setters
	
	public void setRequestPathInfo(String requestPathInfo) {
		this.requestPathInfo = requestPathInfo;
	}

	public void setDispatcherPath(String dispatcherPath) {
		this.dispatcherPath = dispatcherPath;
	}

	public void addRequestParameter(String parameterName, Object parameterValue) {
		requestParameterMap.setAttribute(parameterName, parameterValue);
	}
}