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
package org.springframework.webflow;

/**
 * Thrown if an unhandled exception occurs when an action is executed. Typically
 * wraps another exception noting the root cause failure. The root cause may be
 * checked or unchecked.
 * 
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.ActionState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutionException extends FlowExecutionException {

	/**
	 * Create a new action execution exception.
	 * @param flowId the current flow
	 * @param stateId the current state (may be null)
	 * @param action the action that generated an unrecoverable exception
	 * @param executionProperties action execution properties that may have contributed to this failure
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(String flowId, String stateId, Action action,
			UnmodifiableAttributeMap executionProperties, Throwable cause) {
		super(flowId, stateId, "Exception thrown executing " + action + " in state '" + stateId + "' of flow '"
				+ flowId + "' -- action execution properties where '" + executionProperties + "'", cause);
	}

	/**
	 * Create a new action execution exception that occured while a flow
	 * execution was starting and before the start state was entered.
	 * @param flowId the current flow
	 * @param stateId the current state (may be null)
	 * @param action the action that generated an unrecoverable exception
	 * @param executionProperties action execution properties that may have contributed to this failure
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(String flowId, String stateId, Action action,
			UnmodifiableAttributeMap executionProperties, String message, Throwable cause) {
		super(flowId, stateId, message, cause);
	}

}