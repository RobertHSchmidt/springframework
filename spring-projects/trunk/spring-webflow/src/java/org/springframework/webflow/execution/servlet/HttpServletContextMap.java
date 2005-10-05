package org.springframework.webflow.execution.servlet;

import java.util.Collection;
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
		return 0;
	}

	public boolean isEmpty() {
		return context.getAttributeNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return context.getAttribute((String)key) != null;
	}

	public boolean containsValue(Object value) {
		return false;
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
	}

	public void clear() {
	}

	public Set keySet() {
		return null;
	}

	public Collection values() {
		return null;
	}

	public Set entrySet() {
		return null;
	}
}