package org.springframework.webflow.execution.portlet;

import java.util.Enumeration;

import javax.portlet.PortletRequest;

import org.springframework.webflow.util.StringKeyedAttributeMapAdapter;

/**
 * Map backed by the Portlet request parameter map, for accessing request local
 * portlet parameters.
 * @author Keith Donald
 */
public class PortletRequestParameterMap extends StringKeyedAttributeMapAdapter {

	private PortletRequest request;

	public PortletRequestParameterMap(PortletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		return request.getParameter(key);
	}

	protected void setAttribute(String key, Object value) {
		throw new UnsupportedOperationException("PortletRequest parameter maps are immutable");
	}

	protected void removeAttribute(String key) {
		throw new UnsupportedOperationException("PortletRequest parameter maps are immutable");
	}

	protected Enumeration getAttributeNames() {
		return request.getParameterNames();
	}
}