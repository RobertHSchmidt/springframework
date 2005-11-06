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
package org.springframework.webflow.execution;

import java.io.Serializable;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown by execution managers indicating something went wrong.
 * @author Erwin Vervaet
 */
public class FlowExecutionManagementException extends NestedRuntimeException {

	/**
	 * The flow execution identifier.
	 */
	private Serializable flowExecutionId;

	/**
	 * The flow execution.
	 */
	private FlowExecution flowExecution;

	/**
	 * Create a new flow execution management exception.
	 * @param storage the storage strategy involved
	 * @param storageId the unique id of the flow execution (optional)
	 * @param flowExecution the flow execution (optional)
	 * @param message a descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public FlowExecutionManagementException(Serializable flowExecutionId, FlowExecution flowExecution, String message,
			Throwable cause) {
		super(message, cause);
		this.flowExecutionId = flowExecutionId;
		this.flowExecution = flowExecution;
	}

	/**
	 * Returns the storage id of the flow execution.
	 * @return the flow execution storage id
	 */
	public Serializable getFlowExecutionId() {
		return flowExecutionId;
	}

	/**
	 * Returns the flow execution involved. Could be <code>null</code>.
	 * @return the flow execution
	 */
	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}