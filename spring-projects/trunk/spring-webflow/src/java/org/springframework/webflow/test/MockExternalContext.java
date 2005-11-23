package org.springframework.webflow.test;

import java.util.Collections;
import java.util.Map;

import org.springframework.webflow.ExternalContext;

public class MockExternalContext implements ExternalContext {

	private Map parameters;

	public MockExternalContext() {
		this(Collections.EMPTY_MAP);
	}
	
	public MockExternalContext(Map parameters) {
		this.parameters = parameters;
	}

	public Map getRequestParameterMap() {
		return parameters;
	}

	public Map getRequestMap() {
		return null;
	}

	public Map getSessionMap() {
		return null;
	}

	public Map getApplicationMap() {
		return null;
	}
}
