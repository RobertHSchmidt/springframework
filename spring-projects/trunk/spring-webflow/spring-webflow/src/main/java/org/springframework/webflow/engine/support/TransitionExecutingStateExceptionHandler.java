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
package org.springframework.webflow.engine.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.JdkVersion;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewSelection;

/**
 * A flow state exception handler that maps the occurence of a specific type of
 * exception to a transition to a new
 * {@link org.springframework.webflow.engine.State}.
 * 
 * @author Keith Donald
 */
public class TransitionExecutingStateExceptionHandler implements FlowExecutionExceptionHandler {

	private static final Log logger = LogFactory.getLog(TransitionExecutingStateExceptionHandler.class);

	/**
	 * The name of the attribute to expose an handled state exception under in
	 * request scope.
	 */
	public static final String STATE_EXCEPTION_ATTRIBUTE = "stateException";

	/**
	 * The name of the attribute to expose an handled state exception under in
	 * request scope.
	 */
	public static final String ROOT_CAUSE_EXCEPTION_ATTRIBUTE = "rootCauseException";

	/**
	 * The exceptionType->targetStateId map.
	 */
	private Map exceptionTargetStateMappings = new HashMap();

	/**
	 * The list of actions to execute when this handler handles an exception.
	 */
	private ActionList actionList = new ActionList();

	/**
	 * Adds an exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateId the id of the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, String targetStateId) {
		exceptionTargetStateMappings.put(exceptionClass, targetStateId);
		return this;
	}

	public ActionList getActionList() {
		return actionList;
	}

	public boolean handles(FlowExecutionException e) {
		return getTargetStateId(e) != null;
	}

	public ViewSelection handle(FlowExecutionException e, RequestControlContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling state exception " + e);
		}
		context.getRequestScope().put(STATE_EXCEPTION_ATTRIBUTE, e);
		Throwable rootCause = findRootCause(e);
		if (logger.isDebugEnabled()) {
			logger.debug("Exposing state exception root cause " + rootCause + " under attribute '"
					+ ROOT_CAUSE_EXCEPTION_ATTRIBUTE + "'");
		}
		context.getRequestScope().put(ROOT_CAUSE_EXCEPTION_ATTRIBUTE, rootCause);
		actionList.execute(context);
		return context.execute(new Transition(getTargetStateId(e)));
	}

	// helpers

	/**
	 * Find the mapped target state ID for given exception. Returns
	 * <code>null</code> if no mapping can be found for given exception. Will
	 * try all exceptions in the exception cause chain.
	 */
	protected String getTargetStateId(FlowExecutionException e) {
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
		String targetStateId;
		if (isRootCause13(e)) {
			return findTargetStateId(e.getClass());
		}
		else {
			targetStateId = (String)exceptionTargetStateMappings.get(e.getClass());
			if (targetStateId != null) {
				return targetStateId;
			}
			else {
				if (e.getCause() instanceof NestedRuntimeException) {
					return getTargetStateId13((NestedRuntimeException)e.getCause());
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
		String targetStateId;
		if (isRootCause14(t)) {
			return findTargetStateId(t.getClass());
		}
		else {
			targetStateId = (String)exceptionTargetStateMappings.get(t.getClass());
			if (targetStateId != null) {
				return targetStateId;
			}
			else {
				return getTargetStateId14(t.getCause());
			}
		}
	}

	private boolean isRootCause13(NestedRuntimeException t) {
		return t.getCause() == null;
	}

	private boolean isRootCause14(Throwable t) {
		return t.getCause() == null;
	}

	private String findTargetStateId(Class argumentType) {
		while (argumentType != null && argumentType.getClass() != Object.class) {
			if (exceptionTargetStateMappings.containsKey(argumentType)) {
				return (String)exceptionTargetStateMappings.get(argumentType);
			}
			else {
				argumentType = argumentType.getSuperclass();
			}
		}
		return null;
	}

	protected Throwable findRootCause(Throwable e) {
		return new RootCauseResolver().findRootCause(e);
	}

	private static class RootCauseResolver {
		public Throwable findRootCause(Throwable e) {
			if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
				return findRootCause13(e);
			}
			else {
				return findRootCause14(e);
			}
		}

		private Throwable findRootCause13(Throwable e) {
			if (e instanceof NestedRuntimeException) {
				NestedRuntimeException nre = (NestedRuntimeException)e;
				Throwable cause = e.getCause();
				if (cause == null) {
					return nre;
				}
				else {
					return findRootCause13(cause);
				}
			}
			else {
				return e;
			}
		}

		private Throwable findRootCause14(Throwable e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				return e;
			}
			else {
				return findRootCause14(cause);
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("exceptionTargetStateMappings", exceptionTargetStateMappings)
				.toString();
	}
}