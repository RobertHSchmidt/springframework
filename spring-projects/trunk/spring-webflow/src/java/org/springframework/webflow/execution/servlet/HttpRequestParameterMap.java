package org.springframework.webflow.execution.servlet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Map backed by the Servlet HTTP session, for accessing session scoped
 * variables.
 * @author Keith Donald
 */
public class HttpRequestParameterMap extends HashMap implements Map {

	/**
	 * The wrapped http session.
	 */
	private HttpServletRequest request;

	/**
	 * @param request the session
	 */
	public HttpRequestParameterMap(HttpServletRequest request) {
		this.request = request;
	}

	public int size() {
		Enumeration it = request.getParameterNames();
		int i = 0;
		while (it.hasMoreElements()) {
			i++;
			it.nextElement();
		}
		return i;
	}

	public boolean isEmpty() {
		return request.getParameterNames().hasMoreElements();
	}

	public boolean containsKey(Object key) {
		return request.getParameter((String)key) != null;
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return request.getParameter((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
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
		return new EntrySet();
	}
	
	private class EntrySet extends AbstractSet {
		public Iterator iterator() {
			return new EntryIterator();
		}

		public int size() {
			return size();
		}
	}
	
	private class EntryIterator implements Iterator {
		private Enumeration parameterNames = request.getParameterNames();
		
		public boolean hasNext() {
			return parameterNames.hasMoreElements();
		}

		public Object next() {
			final String name = (String)parameterNames.nextElement();
			return new Map.Entry() {
				public Object getKey() {
					return name;
				}

				public Object getValue() {
					return request.getParameter(name);
				}

				public Object setValue(Object arg0) {
					throw new UnsupportedOperationException();
				}
				
			};
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}