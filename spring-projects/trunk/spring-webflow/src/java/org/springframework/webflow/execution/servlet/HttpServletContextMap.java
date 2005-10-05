package org.springframework.webflow.execution.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * Map backed by the servlet context, for accessing application scoped
 * variables.
 * @author Keith Donald
 */
public class HttpServletContextMap implements Map {

	private ServletContext context;

	/**
	 * @param context the servlet context
	 */
	public HttpServletContextMap(ServletContext context) {
		this.context = context;
	}

	public int size() {
		Enumeration it = context.getAttributeNames();
		int i = 0;
		while (it.hasMoreElements()) {
			i++;
			it.nextElement();
		}
		return i;
	}

	public boolean isEmpty() {
		return context.getAttributeNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return context.getAttribute((String)key) != null;
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return context.getAttribute((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		Object old = get(arg0);
		context.setAttribute((String)arg0, arg1);
		return old;
	}

	public Object remove(Object key) {
		Object old = get(key);
		context.removeAttribute((String)key);
		return old;
	}

	public void putAll(Map arg0) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Set keySet() {
		throw new UnsupportedOperationException();
	}

	public Collection values() {
		throw new UnsupportedOperationException();
	}

	public Set entrySet() {
		throw new UnsupportedOperationException();
	}
}