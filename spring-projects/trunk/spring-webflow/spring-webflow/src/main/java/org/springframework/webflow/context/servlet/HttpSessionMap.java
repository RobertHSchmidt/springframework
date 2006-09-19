/*
 * Copyright 2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.context.servlet;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.springframework.web.util.WebUtils;
import org.springframework.webflow.core.collection.AttributeMapBindingEvent;
import org.springframework.webflow.core.collection.AttributeMapBindingListener;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.SharedMap;
import org.springframework.webflow.core.collection.StringKeyedMapAdapter;

/**
 * A Shared Map backed by the Servlet HTTP session, for accessing session scoped
 * attributes.
 * 
 * @author Keith Donald
 */
public class HttpSessionMap extends StringKeyedMapAdapter implements SharedMap {

	/**
	 * The wrapped HTTP request, providing access to the session.
	 */
	private HttpServletRequest request;

	/**
	 * Create a map wrapping the session of given request.
	 */
	public HttpSessionMap(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Internal helper to get the HTTP session associated with the wrapped
	 * request, or null if there is no such session.
	 * <p>
	 * Note that this method will not force session creation.
	 */
	private HttpSession getSession() {
		return request.getSession(false);
	}

	protected Object getAttribute(String key) {
		HttpSession session = getSession();
		if (session == null) {
			return null;
		}
		Object value = session.getAttribute(key);
		if (value instanceof HttpSessionMapBindingListener) {
			return ((HttpSessionMapBindingListener)value).listener;
		} else {
			return value;
		}
	}

	protected void setAttribute(String key, Object value) {
		HttpSession session = request.getSession(true);
		if (value instanceof AttributeMapBindingListener) {
			session.setAttribute(key, new HttpSessionMapBindingListener((AttributeMapBindingListener)value));
		}
		else {
			session.setAttribute(key, value);
		}
	}

	protected void removeAttribute(String key) {
		HttpSession session = getSession();
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	protected Iterator getAttributeNames() {
		HttpSession session = getSession();
		return session == null ? CollectionUtils.EMPTY_ITERATOR : CollectionUtils.toIterator(session
				.getAttributeNames());
	}

	public Object getMutex() {
		HttpSession session = request.getSession(true);
		Object mutex = session.getAttribute(WebUtils.SESSION_MUTEX_ATTRIBUTE);
		return mutex != null ? mutex : session;
	}

	private class HttpSessionMapBindingListener implements HttpSessionBindingListener {
		private AttributeMapBindingListener listener;

		public HttpSessionMapBindingListener(AttributeMapBindingListener listner) {
			this.listener = listner;
		}

		public void valueBound(HttpSessionBindingEvent event) {
			listener.valueBound(getContextBindingEvent(event));
		}

		public void valueUnbound(HttpSessionBindingEvent event) {
			listener.valueUnbound(getContextBindingEvent(event));
		}

		private AttributeMapBindingEvent getContextBindingEvent(HttpSessionBindingEvent event) {
			return new AttributeMapBindingEvent(new LocalAttributeMap(HttpSessionMap.this), event.getName(), listener);
		}
	}
}