package org.springframework.webflow;

import org.springframework.core.NestedRuntimeException;

/**
 * Core base class for exceptions that occur during in a flow state. Provides a
 * reference to the State definition where the exception occured.
 * 
 * @author Keith Donald
 */
public class StateException extends NestedRuntimeException {
	
	/**
	 * The state where the exception occured. 
	 */
	private State state;

	/**
	 * Creates a new state exception.
	 * 
	 * @param state the state where the exception occured
	 * @param message a descriptive message
	 */
	public StateException(State state, String message) {
		super(message);
		this.state = state;
	}

	/**
	 * Creates a new state exception.
	 * 
	 * @param state the state where the exception occured
	 * @param message a descriptive message
	 * @param cause the root cause
	 */
	public StateException(State state, String message, Throwable cause) {
		super(message, cause);
		this.state = state;
	}

	/**
	 * Returns the state where the exception occured.
	 */
	public State getState() {
		return state;
	}
}