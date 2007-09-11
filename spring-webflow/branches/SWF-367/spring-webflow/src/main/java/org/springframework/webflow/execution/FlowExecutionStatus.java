/*
 * Copyright 2004-2007 the original author or authors.
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

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Type-safe enumeration of possible flow execution statuses. Consult the JavaDoc for the {@link FlowSession} for more
 * information on how these statuses are used during the life cycle of a flow session.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionStatus extends StaticLabeledEnum {

	/**
	 * Initial status of a flow execution; the session has been created but not yet activated.
	 */
	public static final FlowExecutionStatus CREATED = new FlowExecutionStatus(0, "Created");

	/**
	 * A flow execution with STARTING status is about to enter its start state.
	 */
	public static final FlowExecutionStatus STARTING = new FlowExecutionStatus(1, "Starting");

	/**
	 * A flow execution with ACTIVE status is currently executing.
	 */
	public static final FlowExecutionStatus ACTIVE = new FlowExecutionStatus(2, "Active");

	/**
	 * A flow execution with PAUSED status is currently waiting on the user to signal an event.
	 */
	public static final FlowExecutionStatus PAUSED = new FlowExecutionStatus(3, "Paused");

	/**
	 * A flow execution that has ENDED is no longer actively executing a flow. This is the final status of a flow
	 * execution.
	 */
	public static final FlowExecutionStatus ENDED = new FlowExecutionStatus(5, "Ended");

	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private FlowExecutionStatus(int code, String label) {
		super(code, label);
	}
}