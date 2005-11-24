package org.springframework.webflow.execution.portlet;

import java.util.Enumeration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;
import org.springframework.webflow.util.EmptyEnumeration;

/**
 * Map backed by the Portlet session, for accessing session scoped attributes.
 * @author Keith Donald
 */
public class PortletSessionMap extends AbstractStringKeyedAttributeMap {

	/**
	 * The wrapped portlet request, providing access to the session.
	 */
	private PortletRequest request;

	public PortletSessionMap(PortletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		PortletSession session = getSession();
		return (session == null) ? null : session.getAttribute(key);
	}

	private PortletSession getSession() {
		return request.getPortletSession(false);
	}

	protected void setAttribute(String key, Object value) {
		request.getPortletSession(true).setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		PortletSession session = getSession();
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	protected Enumeration getAttributeNames() {
		PortletSession session = getSession();
		return (session == null) ? EmptyEnumeration.INSTANCE : session.getAttributeNames();
	}
}