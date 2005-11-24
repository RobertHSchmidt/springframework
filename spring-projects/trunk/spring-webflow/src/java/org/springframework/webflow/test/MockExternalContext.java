package org.springframework.webflow.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.webflow.ExternalContext;

/**
 * Mock implementation of the <code>ExternalContext</code> interface.
 * 
 * @author Keith Donald
 */
public class MockExternalContext implements ExternalContext {

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
	 * Creates a mock external context with a single parameter in the request parameter map.
	 * @param singleRequestParameterName the parameter name
	 * @param singleRequestParameterValue the parameter value
	 */	
	public MockExternalContext(String singleRequestParameterName, Object singleRequestParameterValue) {
		this.requestParameterMap = new HashMap(1);
		this.requestParameterMap.put(singleRequestParameterName, singleRequestParameterValue);
	}
	
	/**
	 * Creates a mock external context with the specified parameters in the request parameter map.
	 * @param requestParameterMap the request parameter map
	 */
	public MockExternalContext(Map requestParameterMap) {
		this.requestParameterMap = requestParameterMap;
	}

	public Map getRequestParameterMap() {
		return requestParameterMap;
	}

	public Map getRequestMap() {
		return requestMap;
	}

	public Map getSessionMap() {
		return sessionMap;
	}

	public Map getApplicationMap() {
		return applicationMap;
	}
}