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
import java.util.Map;

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
	private Map exceptionTargetStateResolverMapping = new HashMap();

	/**
	 * Adds an exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateId the id of the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, String targetStateId) {
		return add(exceptionClass, new DefaultTargetStateResolver(targetStateId));
	}

	/**
	 * Adds a exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateResolver the resolver to calculate the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, TargetStateResolver targetStateResolver) {
		Assert.notNull(exceptionClass, "The exception class is required");
		Assert.notNull(targetStateResolver, "The target state resolver is required");
		exceptionTargetStateResolverMapping.put(exceptionClass, targetStateResolver);
		return this;
	}

	public boolean handles(StateException e) {
		return getTargetStateResolver(e) != null;
	}

	public ViewSelection handle(StateException e, FlowExecutionControlContext context) {
		State sourceState = context.getCurrentState();
		if (!(sourceState instanceof TransitionableState)) {
			throw new IllegalStateException("The source state '" + sourceState.getId()
					+ "' to transition from must be transitionable!");
		}
		context.getRequestScope().put(HANDLED_STATE_EXCEPTION_ATTRIBUTE, e);
		return new Transition(getTargetStateResolver(e)).execute((TransitionableState)sourceState, context);
	}

	// helpers

	/**
	 * Find the mapped target state ID for given exception. Returns
	 * <code>null</code> if no mapping can be found for given exception. Will
	 * try all exceptions in the exception cause chain.
	 */
	protected TargetStateResolver getTargetStateResolver(StateException e) {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
			return getTargetStateResolver13(e);
		}
		else {
			return getTargetStateResolver14(e);
		}
	}

	/**
	 * Internal getTargetState implementation for use with JDK 1.3.
	 */
	private TargetStateResolver getTargetStateResolver13(NestedRuntimeException e) {
		if (exceptionTargetStateResolverMapping.containsKey(e.getClass())) {
			return (TargetStateResolver)exceptionTargetStateResolverMapping.get(e.getClass());
		}
		else {
			Throwable throwable = e.getCause();
			if (throwable != null && throwable instanceof NestedRuntimeException) {
				return getTargetStateResolver13((NestedRuntimeException)throwable);
			}
			else {
				if (exceptionTargetStateResolverMapping.containsKey(throwable.getClass())) {
					return (TargetStateResolver)exceptionTargetStateResolverMapping.get(throwable.getClass());
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
	private TargetStateResolver getTargetStateResolver14(Throwable t) {
		if (exceptionTargetStateResolverMapping.containsKey(t.getClass())) {
			return (TargetStateResolver)exceptionTargetStateResolverMapping.get(t.getClass());
		}
		else {
			if (t.getCause() != null) {
				return getTargetStateResolver14(t.getCause());
			}
			else {
				return null;
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("exceptionStateMap", exceptionTargetStateResolverMapping).toString();
	}
}