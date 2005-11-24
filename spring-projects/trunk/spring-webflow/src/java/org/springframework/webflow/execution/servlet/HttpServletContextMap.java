package org.springframework.webflow.execution.servlet;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.springframework.webflow.util.AbstractStringKeyedAttributeMap;

/**
 * Map backed by the Servlet context, for accessing application scoped
 * variables.
 * @author Keith Donald
 */
public class HttpServletContextMap extends AbstractStringKeyedAttributeMap {

	/**
	 * The wrapped servlet context.
	 */
	private ServletContext context;

	/**
	 * @param context the servlet context
	 */
	public HttpServletContextMap(ServletContext context) {
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