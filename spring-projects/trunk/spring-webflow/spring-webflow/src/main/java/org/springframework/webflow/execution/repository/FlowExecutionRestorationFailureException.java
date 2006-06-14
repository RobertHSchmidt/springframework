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
package org.springframework.webflow.execution.repository;

/**
 * Thrown when the flow execution with the persistent identifier provided could
 * not be restored. This could occur if the execution has been removed from the
 * repository and a client still has a handle to the key.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionRestorationFailureException extends FlowExecutionRepositoryException {

	/**
	 * The key of the execution that could not be restored.
	 */
	private FlowExecutionKey flowExecutionKey;

	/**
	 * Creates a new flow execution restoration exception.
	 * @param flowExecutionKey the key of the execution that could not be
	 * restored.
	 * @param cause the root cause of the restoration failure.
	 */
	public FlowExecutionRestorationFailureException(FlowExecutionKey flowExecutionKey, Exception cause) {
		super("Unable to restore flow execution with key '" + flowExecutionKey
				+ "' -- perhaps this executing flow has ended or expired? "
				+ "This could happen if your users are relying on browser history "
				+ "(typically via the back button) that reference ended flows.", cause);
		this.flowExecutionKey = flowExecutionKey;
	}

	/**
	 * Returns key of the flow execution that could not be restored.
	 */
	public FlowExecutionKey getFlowExecutionKey() {
		return flowExecutionKey;
	}
}