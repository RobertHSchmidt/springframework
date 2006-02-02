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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.SharedMapDecorator;

/**
 * Mock implementation of the <code>ExternalContext</code> interface.
 * 
 * @author Keith Donald
 */
public class MockExternalContext implements ExternalContext {

	private String dispatcherPath;

	private String requestPathInfo;

	private Map requestParameterMap = Collections.EMPTY_MAP;

	private Map requestMap = new TreeMap();

	private Map sessionMap = new TreeMap();

	private Map applicationMap = new TreeMap();

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
		requestParameterMap = new HashMap(1);
		requestParameterMap.put(singleRequestParameterName, singleRequestParameterValue);
	}

	/**
	 * Creates a mock external context with the specified parameters in the
	 * request parameter map.
	 * @param requestParameterMap the request parameter map
	 */
	public MockExternalContext(Map requestParameterMap) {
		this.requestParameterMap = Collections.unmodifiableMap(requestParameterMap);
	}

	public String getDispatcherPath() {
		return dispatcherPath;
	}

	public void setDispatcherPath(String dispatcherPath) {
		this.dispatcherPath = dispatcherPath;
	}

	public String getRequestPathInfo() {
		return requestPathInfo;
	}

	public void setRequestPathInfo(String requestPathInfo) {
		this.requestPathInfo = requestPathInfo;
	}

	public Map getRequestParameterMap() {
		return requestParameterMap;
	}

	public Map getRequestMap() {
		return requestMap;
	}

	public SharedMap getSessionMap() {
		return new MockSharedMapDecorator(sessionMap);
	}

	public SharedMap getApplicationMap() {
		return new MockSharedMapDecorator(applicationMap);
	}

	private static class MockSharedMapDecorator extends SharedMapDecorator {
		private Mutex mutex = new Mutex();

		public MockSharedMapDecorator(Map map) {
			super(map);
		}

		public Object getMutex() {
			return mutex;
		}
	}

	/**
	 * A simple mock mutex.
	 */
	private static class Mutex implements Serializable {
	}
}