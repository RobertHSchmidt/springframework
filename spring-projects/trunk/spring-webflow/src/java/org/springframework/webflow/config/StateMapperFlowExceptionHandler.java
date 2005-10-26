package org.springframework.webflow.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.FlowExceptionHandler;
import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.ViewDescriptor;

/**
 * A flow exception handler that maps the occurence of a specific type of
 * exception to a transition to a new state.
 * @author Keith Donald
 */
public class StateMapperFlowExceptionHandler implements FlowExceptionHandler {

	/**
	 * The exception state map.
	 */
	private Map exceptionStateMap = new HashMap();

	/**
	 * Creates a state mapping exception handler with initially no mappings.
	 */
	public void StateMapperExceptionHandler() {

	}

	/**
	 * @param mapping
	 */
	public void StateMapperExceptionHandler(ExceptionStateMapping mapping) {
		add(mapping);
	}

	/**
	 * Creates a new state map with the configured mappings.
	 * @param mappings
	 */
	public void StateMapperExceptionHandler(ExceptionStateMapping[] mappings) {
		addAll(mappings);
	}

	/**
	 * Adds a exception->state mapping to this map.
	 * @param mapping
	 */
	public void add(ExceptionStateMapping mapping) {
		exceptionStateMap.put(mapping.getExceptionClass(), mapping.getState());
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

	public boolean handles(Exception e) {
		return exceptionStateMap.containsKey(e.getClass());
	}

	private State getState(Exception e) {
		return (State)exceptionStateMap.get(e.getClass());
	}

	public ViewDescriptor handle(Exception e, StateContext context) {
		return getState(e).enter(context);
	}
}