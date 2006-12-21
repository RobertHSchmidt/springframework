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
package org.springframework.webflow.execution;

import org.springframework.webflow.FlowException;

/**
 * Base class for flow locator exceptions.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class FlowLocatorException extends FlowException {

	/**
	 * The id of the flow that could not be located.
	 */
	private String flowId;

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow id
	 * @param message the exception message
	 */
	public FlowLocatorException(String flowId, String message) {
		this(flowId, message, null);
	}

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow id
	 * @param message the exception message
	 */
	public FlowLocatorException(String flowId, String message, Throwable cause) {
		super(message, cause);
		this.flowId = flowId;
	}

	/**
	 * Returns the id of the flow definition that could not be found.
	 */
	public String getFlowId() {
		return flowId;
	}
}