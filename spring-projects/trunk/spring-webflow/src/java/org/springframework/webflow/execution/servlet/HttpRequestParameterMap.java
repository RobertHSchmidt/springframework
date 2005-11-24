package org.springframework.webflow.execution.servlet;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.util.StringKeyedAttributeMapAdapter;

/**
 * Map backed by the Servlet HTTP request parameter map, for accessing request
 * parameters.
 * @author Keith Donald
 */
public class HttpRequestParameterMap extends StringKeyedAttributeMapAdapter {

	/**
	 * The wrapped http request.
	 */
	private HttpServletRequest request;

	public HttpRequestParameterMap(HttpServletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		return request.getParameter(key);
	}

	protected void setAttribute(String key, Object value) {
		throw new UnsupportedOperationException("HttpServletRequest parameter maps are immutable");
	}

	protected void removeAttribute(String key) {
		throw new UnsupportedOperationException("HttpServletRequest parameter maps are immutable");
	}

	protected Enumeration getAttributeNames() {
		return request.getParameterNames();
	}
}