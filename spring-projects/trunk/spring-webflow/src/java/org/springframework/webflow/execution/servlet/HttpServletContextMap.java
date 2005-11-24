package org.springframework.webflow.execution.servlet;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.springframework.webflow.util.StringKeyedAttributeMapAdapter;

/**
 * Map backed by the Servlet context, for accessing application scoped
 * attributes.
 * @author Keith Donald
 */
public class HttpServletContextMap extends StringKeyedAttributeMapAdapter {

	/**
	 * The wrapped servlet context.
	 */
	private ServletContext context;

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