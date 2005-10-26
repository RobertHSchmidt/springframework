package org.springframework.webflow;

/**
 * A strategy for handling an exception that occurs during the execution of a
 * flow definition.
 * @author Keith Donald
 */
public interface FlowExceptionHandler {

	/**
	 * Can this handler handle the given exception?
	 * @param e the exception
	 * @return true if yes, false if no
	 */
	public boolean handles(Exception e);

	/**
	 * Handle this exception in the state context of the current request.
	 * @param e the exception
	 * @param context the state context
	 * @return the selected view
	 */
	public ViewDescriptor handle(Exception e, StateContext context);
}
