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
package org.springframework.webflow;

/**
 * Provides contextual information about an actively executing flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionContext extends FlowExecutionStatistics {

	/**
	 * Returns the root flow definition associated with this executing flow.
	 * @return the root flow definition
	 */
	public Flow getRootFlow();

	/**
	 * Returns the definition for the flow that is currently executing.
	 * @return the flow definition for the active session
	 * @throws IllegalStateException when this flow is no longer actively executing
	 */
	public Flow getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the current state of the executing flow.
	 * @return the current state
	 * @throws IllegalStateException when this flow is no longer actively executing
	 */
	public State getCurrentState() throws IllegalStateException;

	/**
	 * Returns the active flow session.
	 * @return the active flow session
	 * @throws IllegalStateException when this flow is no longer actively executing
	 */
	public FlowSession getActiveSession() throws IllegalStateException;
}