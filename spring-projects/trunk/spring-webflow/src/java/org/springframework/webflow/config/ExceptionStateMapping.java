package org.springframework.webflow.config;

import java.io.Serializable;

import org.springframework.webflow.State;

/**
 * A mapping between a class of exception and a flow state. Used to determine
 * what state to transition to if a specific class of exception occurs during a
 * flow execution.
 * @author Keith Donald
 */
public class ExceptionStateMapping implements Serializable {

	/**
	 * The exception class.
	 */
	private Class exceptionClass;

	/**
	 * The state type.
	 */
	private State state;

	/**
	 * Creates a new exception->state mapping.
	 */
	public ExceptionStateMapping(Class exceptionClass, State state) {
		this.exceptionClass = exceptionClass;
		this.state = state;
	}

	public Class getExceptionClass() {
		return exceptionClass;
	}

	public State getState() {
		return state;
	}
}
