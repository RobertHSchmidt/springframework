package org.springframework.webflow;

import java.util.Map;

/**
 * <p>
 * Controller interface that takes control from the
 * {@link org.springframework.webflow.execution.FlowExecutionManager}.
 * 
 * <p>
 * Controller implementations can render the view themselves. Good controller
 * citizens however do not bind to the container environment (servlet or
 * portlet) and thus do not render a view. Instead they delegate to a view class
 * that for example renders XML based on model values.
 * 
 * <p>
 * The controller can return a model that will be added to the request scope of
 * the request context.
 * 
 * @author Steven Devijver
 * @since 10-08-2005
 */
public interface Controller {

	/**
	 * <p>
	 * Implements the controller logic that optionally renders a view.
	 * 
	 * <p>
	 * If no view is rendered the controller may return a model that will
	 * be added to the request scope of the request context.
	 * 
	 * @param flowScope
	 * @param event
	 * @return optionally a model.
	 */
	public Map handle(Scope flowScope, Event event);
}
