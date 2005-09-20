package org.springframework.webflow.execution.servlet;

import javax.servlet.http.HttpSession;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.ExternalScopeAccessor;

public class HttpSessionScopeAccessor implements ExternalScopeAccessor {
	public MutableAttributeSource getScope(Event sourceEvent, boolean createScope) {
		return new HttpSessionAttributeSource(ServletEvent.getRequest(sourceEvent).getSession(createScope));
	}

	private static class HttpSessionAttributeSource implements MutableAttributeSource {
		private HttpSession session;

		public HttpSessionAttributeSource(HttpSession session) {
			this.session = session;
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

		public boolean containsAttribute(String attributeName) {
			return session.getAttribute(attributeName) != null;
		}

		public Object getAttribute(String attributeName) {
			return session.getAttribute(attributeName);
		}
	}
}