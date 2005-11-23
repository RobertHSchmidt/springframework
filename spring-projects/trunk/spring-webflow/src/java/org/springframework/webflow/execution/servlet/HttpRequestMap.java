package org.springframework.webflow.execution.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Map backed by the Servlet HTTP session, for accessing session scoped
 * variables.
 * @author Keith Donald
 */
public class HttpRequestMap implements Map {

	/**
	 * The wrapped http session.
	 */
	private HttpServletRequest request;

	/**
	 * @param request the session
	 */
	public HttpRequestMap(HttpServletRequest request) {
		this.request = request;
	}

	public int size() {
		Enumeration it = request.getAttributeNames();
		int i = 0;
		while (it.hasMoreElements()) {
			i++;
			it.nextElement();
		}
		return i;
	}

	public boolean isEmpty() {
		return request.getAttributeNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return request.getAttribute((String)key) != null;
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return request.getAttribute((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		Object old = get(arg0);
		request.setAttribute((String)arg0, arg1);
		return old;
	}

	public Object remove(Object key) {
		Object old = get(key);
		request.removeAttribute((String)key);
		return old;
	}

	public void putAll(Map arg0) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Set keySet() {
		// TODO
		throw new UnsupportedOperationException();
	}

	public Collection values() {
		// TODO
		throw new UnsupportedOperationException();
	}

	public Set entrySet() {
		// TODO
		throw new UnsupportedOperationException();
	}
}