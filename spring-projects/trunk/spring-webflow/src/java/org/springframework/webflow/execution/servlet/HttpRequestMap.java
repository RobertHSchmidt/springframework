package org.springframework.webflow.execution.servlet;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;

/**
 * Map backed by the Servlet HTTP request attribute map, for accessing request
 * local attributes.
 * @author Keith Donald
 */
public class HttpRequestMap extends AbstractStringKeyedAttributeMap {

	private HttpServletRequest request;

	public HttpRequestMap(HttpServletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		return request.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		request.removeAttribute(key);
	}

	protected Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}
}