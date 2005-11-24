package org.springframework.webflow.test;

import java.util.Collections;
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
	
	public MockExternalContext() {
		
	}
	
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