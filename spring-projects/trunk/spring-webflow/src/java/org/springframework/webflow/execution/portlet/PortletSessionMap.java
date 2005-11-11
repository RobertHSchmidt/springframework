package org.springframework.webflow.execution.portlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletSession;

/**
 * Map backed by the Portlet session, for accessing session scoped variables.
 * @author Keith Donald
 */
public class PortletSessionMap implements Map {

	/**
	 * The wrapped http session.
	 */
	private PortletSession session;

	/**
	 * @param session the session
	 */
	public PortletSessionMap(PortletSession session) {
		this.session = session;
	}

	public int size() {
		Enumeration it = session.getAttributeNames();
		int i = 0;
		while (it.hasMoreElements()) {
			i++;
			it.nextElement();
		}
		return i;
	}

	public boolean isEmpty() {
		return session.getAttributeNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return session.getAttribute((String)key) != null;
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return session.getAttribute((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		Object old = get(arg0);
		session.setAttribute((String)arg0, arg1);
		return old;
	}

	public Object remove(Object key) {
		Object old = get(key);
		session.removeAttribute((String)key);
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