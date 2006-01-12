/**
 * 
 */
package org.springframework.webflow.manager.jsf;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;

public class FlowExecutionHolder implements Serializable {

	private FlowExecutionContinuationKey continuationKey;

	private FlowExecution flowExecution;

	public FlowExecutionHolder(FlowExecution flowExecution) {
		this.flowExecution = flowExecution;
	}

	public FlowExecutionHolder(FlowExecutionContinuationKey continuationKey, FlowExecution flowExecution) {
		this.continuationKey = continuationKey;
		this.flowExecution = flowExecution;
	}

	public FlowExecutionContinuationKey getContinuationKey() {
		return continuationKey;
	}

	public void setContinuationKey(FlowExecutionContinuationKey continuationKey) {
		this.continuationKey = continuationKey;
	}

	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}