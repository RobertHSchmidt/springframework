package org.springframework.webflow.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;

/**
 * A flow state exception handler that maps the occurence of a specific type of
 * exception to a transition to a new {@link org.springframework.webflow.State}
 * 
 * @author Keith Donald
 */
public class TransitionExecutorStateExceptionHandler implements StateExceptionHandler {

	/**
	 * The exception->targetState map.
	 */
	private Map exceptionStateMap = new HashMap();

	/**
	 * Creates a state mapping exception handler with initially no mappings.
	 */
	public TransitionExecutorStateExceptionHandler() {

	}

	/**
	 * @param mapping
	 */
	public TransitionExecutorStateExceptionHandler(ExceptionStateMapping mapping) {
		add(mapping);
	}

	/**
	 * Creates a new state map with the configured mappings.
	 * @param mappings
	 */
	public TransitionExecutorStateExceptionHandler(ExceptionStateMapping[] mappings) {
		addAll(mappings);
	}

	/**
	 * Adds a exception->state mapping to this map.
	 * @param mapping
	 */
	public void add(ExceptionStateMapping mapping) {
		exceptionStateMap.put(mapping.getExceptionClass(), mapping.getTargetState());
	}

	/**
	 * Adds the exception->state mappings to this map.
	 * @param mappings the mappings
	 */
	public void addAll(ExceptionStateMapping[] mappings) {
		for (int i = 0; i < mappings.length; i++) {
			add(mappings[i]);
		}
	}

	public boolean handles(StateException e) {
		if (exceptionStateMap.containsKey(e.getClass())) {
			return true;
		}
		else {
			Throwable cause = e.getCause();
			while (cause != null) {
				if (exceptionStateMap.containsKey(cause.getClass())) {
					return true;
				}
				cause = cause.getCause();
			}
			return false;
		}
	}

	private State getTargetState(StateException e) {
		if (exceptionStateMap.containsKey(e.getClass())) {
			return (State)exceptionStateMap.get(e.getClass());
		}
		else {
			Throwable cause = e.getCause();
			while (cause != null) {
				if (exceptionStateMap.containsKey(cause.getClass())) {
					return (State)exceptionStateMap.get(cause.getClass());
				}
				cause = cause.getCause();
			}
			throw new IllegalStateException("Should not happen");
		}
	}

	public ViewSelection handle(StateException e, StateContext context) {
		State sourceState = context.getFlowExecutionContext().getCurrentState();
		if (!sourceState.isTransitionable()) {
			throw new IllegalStateException("The source state: '" + sourceState.getId()
					+ "' to transition from on exception: [" + e + "] must be transitionable!");
		}
		return new Transition((TransitionableState)sourceState, getTargetState(e)).execute(context);
	}
}