package org.springframework.webflow.execution.portlet;

import java.util.Enumeration;

import javax.portlet.PortletContext;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;

/**
 * Map backed by the Portlet context, for accessing application scoped
 * attributes.
 * @author Keith Donald
 */
public class PortletContextMap extends AbstractStringKeyedAttributeMap {

	/**
	 * The wrapped servlet context.
	 */
	private PortletContext context;

	public PortletContextMap(PortletContext context) {
		this.context = context;
	}

	protected Object getAttribute(String key) {
		return context.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		context.setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		context.removeAttribute(key);
	}

	protected Enumeration getAttributeNames() {
		return context.getAttributeNames();
	}

}