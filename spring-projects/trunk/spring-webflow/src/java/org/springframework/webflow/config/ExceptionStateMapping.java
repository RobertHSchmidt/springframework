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
	 * The class of exception that may occur.
	 */
	private Class exceptionClass;

	/**
	 * The state to transition to if it does occur.
	 */
	private State targetState;

	/**
	 * Creates a new exception->state mapping.
	 */
	public ExceptionStateMapping(Class exceptionClass, State targetState) {
		this.exceptionClass = exceptionClass;
		this.targetState = targetState;
	}

	public Class getExceptionClass() {
		return exceptionClass;
	}

	public State getTargetState() {
		return targetState;
	}
}
