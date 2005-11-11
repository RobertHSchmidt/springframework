package org.springframework.webflow.execution.portlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletContext;

/**
 * Map backed by the Portlet context, for accessing application scoped
 * variables.
 * @author Keith Donald
 */
public class PortletContextMap implements Map {

	/**
	 * The wrapped servlet context.
	 */
	private PortletContext context;

	/**
	 * @param context the servlet context
	 */
	public PortletContextMap(PortletContext context) {
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
		return !context.getAttributeNames().hasMoreElements();
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