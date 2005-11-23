package org.springframework.webflow.execution.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Map backed by the Servlet HTTP session, for accessing session scoped
 * variables.
 * @author Keith Donald
 */
public class HttpRequestParameterMap implements Map {

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
		String[] values = request.getParameterValues(null);
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return true;
			}
		}
		return false;
	}

	public Object get(Object key) {
		return request.getParameter((String)key);
	}

	public Object put(Object arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException("You cannot modify an immutable parameter map");
	}

	public void putAll(Map arg0) {
		throw new UnsupportedOperationException("You cannot modify an immutable parameter map");
	}

	public void clear() {
		throw new UnsupportedOperationException("You cannot modify an immutable parameter map");
	}

	public Set keySet() {
		return new KeySet();
	}

	public Collection values() {
		return new ValueSet();
	}

	public Set entrySet() {
		return new EntrySet();
	}

	private abstract class AbstractSet extends java.util.AbstractSet {
		public int size() {
			return size();
		}
	}

	private class KeySet extends AbstractSet {
		public Iterator iterator() {
			return new KeyIterator();
		}
	}

	private class ValueSet extends AbstractSet {
		public Iterator iterator() {
			return new ValueIterator();
		}
	}

	private class EntrySet extends AbstractSet {
		public Iterator iterator() {
			return new EntryIterator();
		}
	}

	private abstract class AbstractIterator implements Iterator {
		protected Enumeration parameterNames = request.getParameterNames();

		public boolean hasNext() {
			return parameterNames.hasMoreElements();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class KeyIterator extends AbstractIterator {
		public Object next() {
			return parameterNames.nextElement();
		}
	}

	private class ValueIterator extends AbstractIterator {
		public Object next() {
			String name = (String)parameterNames.nextElement();
			return request.getParameter(name);
		}
	}

	private class EntryIterator extends AbstractIterator {
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
	}
}