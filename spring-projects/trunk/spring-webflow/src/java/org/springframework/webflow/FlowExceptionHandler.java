package org.springframework.webflow;

/**
 * A strategy for handling an exception that occurs during the execution of a
 * flow definition.
 * @author Keith Donald
 */
public interface FlowExceptionHandler {

	/**
	 * Can this handler handle the given exception?
	 * @param e the exception that occured
	 * @return true if yes, false if no
	 */
	public boolean handles(Exception e);

	/**
	 * Handle this exception in the state context of the current request and
	 * optionally select an error view that should be displayed.
	 * @param e the exception that occured
	 * @param context the state context
	 * @return the selected error view that should be displayed (may be null if
	 * the handler chooses not to select a view)
	 */
	public ViewDescriptor handle(Exception e, StateContext context);
}
