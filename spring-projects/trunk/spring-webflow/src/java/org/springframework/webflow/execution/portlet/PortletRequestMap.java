package org.springframework.webflow.execution.portlet;

import java.util.Enumeration;

import javax.portlet.PortletRequest;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;

/**
 * Map backed by the Portlet request, for accessing request scoped attributes.
 * @author Keith Donald
 */
public class PortletRequestMap extends AbstractStringKeyedAttributeMap {

	/**
	 * The wrapped http session.
	 */
	private PortletRequest request;

	/**
	 * @param request the session
	 */
	public PortletRequestMap(PortletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		return request.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		request.removeAttribute(key);
	}

	protected Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}
}