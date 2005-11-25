/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;

/**
 * A flow state exception handler that maps the occurence of a specific type of
 * exception to a transition to a new {@link org.springframework.webflow.State}.
 * 
 * @author Keith Donald
 */
public class TransitionExecutingStateExceptionHandler implements StateExceptionHandler {

	/**
	 * The exceptionType->targetState map.
	 */
	private Map exceptionStateMap = new HashMap();

	/**
	 * Creates a state mapping exception handler with initially no mappings.
	 */
	public TransitionExecutingStateExceptionHandler() {
	}

	/**
	 * Adds a exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetState the state to transition to if the specified type of
	 * exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, State targetState) {
		exceptionStateMap.put(exceptionClass, targetState);
		return this;
	}

	/**
	 * Adds the given exception->state mappings to this handler.
	 * @param mappings the mappings to add
	 */
	public void addAll(Map mappings) {
		for (Iterator entries=mappings.entrySet().iterator(); entries.hasNext(); ) {
			Entry entry = (Entry)entries.next();
			add((Class)entry.getKey(), (State)entry.getValue());
		}
	}

	public boolean handles(StateException e) {
		return getTargetState(e) != null;
	}

	public ViewSelection handle(StateException e, FlowExecutionControlContext context) {
		State sourceState = context.getFlowExecutionContext().getCurrentState();
		if (!sourceState.isTransitionable()) {
			throw new IllegalStateException("The source state '" + sourceState.getId()
					+ "' to transition from must be transitionable!");
		}
		return new Transition((TransitionableState)sourceState, getTargetState(e)).execute(context);
	}
	
	//helpers
	
	/**
	 * Find the mapped target state for given exception. Returns null
	 * if no mapping can be found for given exception. Will try all
	 * exceptions in the exception cause chain.
	 */
	protected State getTargetState(Throwable t) {
		if (exceptionStateMap.containsKey(t.getClass())) {
			return (State)exceptionStateMap.get(t.getClass());
		}
		else {
			if (t.getCause() != null) {
				return getTargetState(t.getCause());
			}
			else {
				return null;
			}
		}
	}
}