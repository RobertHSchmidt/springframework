package org.springframework.webflow.execution.servlet;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;

/**
 * Map backed by the Servlet HTTP session, for accessing session scoped
 * variables.
 * @author Keith Donald
 */
public class HttpSessionMap extends AbstractStringKeyedAttributeMap {

	/**
	 * The wrapped http session.
	 */
	private HttpServletRequest request;

	/**
	 * @param session the session
	 */
	public HttpSessionMap(HttpServletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		HttpSession session = getSession();
		return (session == null) ? null : session.getAttribute(key);
	}

	private HttpSession getSession() {
		return request.getSession(false);
	}

	protected void setAttribute(String key, Object value) {
		request.getSession(true).setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		HttpSession session = getSession();
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	protected Enumeration getAttributeNames() {
		HttpSession session = getSession();
		return (session == null) ? EmptyEnumeration.INSTANCE : session.getAttributeNames();
	}
}