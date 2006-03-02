/*
 * Copyright 2002-2006 the original author or authors.
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

import org.springframework.core.JdkVersion;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
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
	 * The name of the attribute to expose an handled state exception under in
	 * request scope.
	 */
	public static final String HANDLED_STATE_EXCEPTION_ATTRIBUTE = "handledStateException";

	/**
	 * The exceptionType->targetStateId map.
	 */
	private Map exceptionTargetStateIdMapping = new HashMap();

	/**
	 * Creates a state mapping exception handler with initially no mappings.
	 */
	public TransitionExecutingStateExceptionHandler() {
	}

	/**
	 * Adds a exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateId the id of the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, String targetStateId) {
		Assert.notNull(exceptionClass, "The exception class is required");
		Assert.hasText(targetStateId, "The target state id is required");
		exceptionTargetStateIdMapping.put(exceptionClass, targetStateId);
		return this;
	}

	/**
	 * Adds the given exception->state mappings to this handler.
	 * @param mappings the mappings to add
	 */
	public void addAll(Map mappings) {
		for (Iterator entries = mappings.entrySet().iterator(); entries.hasNext();) {
			Entry entry = (Entry)entries.next();
			add((Class)entry.getKey(), (String)entry.getValue());
		}
	}

	public boolean handles(StateException e) {
		return getTargetStateId(e) != null;
	}

	public ViewSelection handle(StateException e, FlowExecutionControlContext context) {
		State sourceState = context.getCurrentState();
		if (!(sourceState instanceof TransitionableState)) {
			throw new IllegalStateException("The source state '" + sourceState.getId()
					+ "' to transition from must be transitionable!");
		}
		TargetStateResolver targetStateResolver = new StaticTargetStateResolver(getTargetStateId(e));
		context.getRequestScope().put(HANDLED_STATE_EXCEPTION_ATTRIBUTE, e);
		return new Transition(targetStateResolver).execute((TransitionableState)sourceState, context);
	}

	// helpers

	/**
	 * Find the mapped target state ID for given exception. Returns
	 * <code>null</code> if no mapping can be found for given exception. Will
	 * try all exceptions in the exception cause chain.
	 */
	protected String getTargetStateId(StateException e) {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
			return getTargetStateId13(e);
		}
		else {
			return getTargetStateId14(e);
		}
	}

	/**
	 * Internal getTargetState implementation for use with JDK 1.3.
	 */
	private String getTargetStateId13(NestedRuntimeException e) {
		if (exceptionTargetStateIdMapping.containsKey(e.getClass())) {
			return (String)exceptionTargetStateIdMapping.get(e.getClass());
		}
		else {
			Throwable throwable = e.getCause();
			if (throwable != null && throwable instanceof NestedRuntimeException) {
				return getTargetStateId13((NestedRuntimeException)throwable);
			}
			else {
				if (exceptionTargetStateIdMapping.containsKey(throwable.getClass())) {
					return (String)exceptionTargetStateIdMapping.get(throwable.getClass());
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * Internal getTargetState implementation for use with JDK 1.4 or later.
	 */
	private String getTargetStateId14(Throwable t) {
		if (exceptionTargetStateIdMapping.containsKey(t.getClass())) {
			return (String)exceptionTargetStateIdMapping.get(t.getClass());
		}
		else {
			if (t.getCause() != null) {
				return getTargetStateId14(t.getCause());
			}
			else {
				return null;
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("exceptionStateMap", exceptionTargetStateIdMapping).toString();
	}
}