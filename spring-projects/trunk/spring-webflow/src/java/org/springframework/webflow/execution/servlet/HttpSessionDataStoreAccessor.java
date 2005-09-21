/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution.servlet;

import javax.servlet.http.HttpSession;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.DataStoreAccessor;

/**
 * Data store accessor that uses the HTTP session as a data store.
 * This class requires the source events to be ServletEvents.
 * 
 * @see org.springframework.webflow.execution.servlet.ServletEvent
 * 
 * @author Erwin Vervaet
 */
public class HttpSessionDataStoreAccessor implements DataStoreAccessor {
	
	public MutableAttributeSource getDataStore(Event sourceEvent, boolean createDataStore) {
		return new HttpSessionAttributeSource(ServletEvent.getRequest(sourceEvent).getSession(createDataStore));
	}

	/**
	 * Helper class to give access to the HTTP session using the MutableAttributeSource
	 * interface.
	 */
	private static class HttpSessionAttributeSource implements MutableAttributeSource {
		
		private HttpSession session;

		public HttpSessionAttributeSource(HttpSession session) {
			this.session = session;
		}

		public boolean containsAttribute(String attributeName) {
			return session.getAttribute(attributeName) != null;
		}

		public Object getAttribute(String attributeName) {
			return session.getAttribute(attributeName);
		}

		public Object removeAttribute(String attributeName) {
			Object oldValue = session.getAttribute(attributeName);
			session.removeAttribute(attributeName);
			return oldValue;
		}

		public Object setAttribute(String attributeName, Object attributeValue) {
			Object oldValue = session.getAttribute(attributeName);
			session.setAttribute(attributeName, attributeValue);
			return oldValue;
		}
	}
}