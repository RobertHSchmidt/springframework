package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewDescriptorCreator;

/**
 * Simple view descriptor creator that produces a ViewDescriptor with the same
 * view name each time. This producer will make all model data from both
 * flow and request scope available to the view.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SimpleViewDescriptorCreator implements ViewDescriptorCreator, Serializable {

	/**
	 * The static view name to render.
	 */
	private String viewName;

	/**
	 * Default constructor for bean style usage.
	 */
	public SimpleViewDescriptorCreator() {
	}
	
	/**
	 * Creates a view descriptor creator that will produce view descriptors requesting that the
	 * specified view is rendered.
	 * @param viewName the view name
	 */
	public SimpleViewDescriptorCreator(String viewName) {
		setViewName(viewName);
	}
	
	/**
	 * Returns the name of the view that should be rendered.
	 */
	public String getViewName() {
		return this.viewName;
	}
	
	/**
	 * Set the name of the view that should be rendered.
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	
	public ViewDescriptor createViewDescriptor(RequestContext context) {
		return new ViewDescriptor(getViewName(), context.getModel());
	}
	
	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).toString();
	}
}