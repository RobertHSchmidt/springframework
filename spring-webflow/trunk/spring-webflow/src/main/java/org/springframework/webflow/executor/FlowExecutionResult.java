/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.executor;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.Event;

/**
 * A value object providing information about the result of a flow execution request.
 * 
 * @author Keith Donald
 */
public class FlowExecutionResult {

	private String flowId;

	private String flowExecutionKey;

	private String outcome;

	private AttributeMap output;

	private FlowExecutionResult(String flowId, String flowExecutionKey, String outcome, AttributeMap output) {
		this.flowId = flowId;
		this.flowExecutionKey = flowExecutionKey;
		this.outcome = outcome;
		this.output = output;
	}

	/**
	 * Factory method that creates a paused result, indicating the flow is now in a wait state after handling the
	 * request.
	 * @param flowId the flow id
	 * @param flowExecutionKey the flow execution key
	 * @return the result
	 */
	public static FlowExecutionResult createPausedResult(String flowId, String flowExecutionKey) {
		return new FlowExecutionResult(flowId, flowExecutionKey, null, null);
	}

	/**
	 * Factory method that creates a ended result, indicating the flow terminated after handling the request.
	 * @param flowId the flow id
	 * @param outcome the ending execution outcome
	 * @return the result
	 */
	public static FlowExecutionResult createEndedResult(String flowId, Event outcome) {
		return new FlowExecutionResult(flowId, null, outcome.getId(), outcome.getAttributes());
	}

	/**
	 * Returns the flow definition that completed execution.
	 * @return the flow id
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Returns true if the flow execution paused and is now in a wait state.
	 * @return true if paused, false if not
	 */
	public boolean paused() {
		return flowExecutionKey != null;
	}

	/**
	 * Returns the key needed to resume the flow execution when a paused result.
	 * @see #paused()
	 * @return the key of the paused flow execution
	 */
	public String getPausedKey() {
		return flowExecutionKey;
	}

	/**
	 * Returns true if the flow execution ended.
	 * @return true if ended, false if not
	 */
	public boolean ended() {
		return flowExecutionKey == null;
	}

	/**
	 * Returns the flow execution outcome when ab ended result.
	 * @see #ended()
	 * @return the ended outcome
	 */
	public String getEndedOutcome() {
		return outcome;
	}

	/**
	 * Returns the output returned from the flow execution when an ended result.
	 * @see #ended()
	 * @return the ended output
	 */
	public AttributeMap getEndedOutput() {
		return output;
	}

}
