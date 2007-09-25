package org.springframework.webflow.engine.builder.support;

import org.springframework.webflow.execution.ViewFactory;

public interface ViewFactoryCreator {

	/**
	 * Create and return a view factory
	 * @param viewName the name of the view to be produced by the created view factory
	 * @return the view factory
	 */
	ViewFactory create(String viewName);

}
