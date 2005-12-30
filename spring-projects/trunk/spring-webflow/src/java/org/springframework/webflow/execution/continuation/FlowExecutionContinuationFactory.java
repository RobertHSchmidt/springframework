package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A factory for creating different FlowExecution implementations.
 * @author Keith Donald
 */
public interface FlowExecutionContinuationFactory {

	/**
	 * Creates a new flow execution continuation.
	 * @param key the flowExecution key
	 * @param flowExecution the flow execution
	 * @return the continuation
	 */
	public FlowExecutionContinuation createContinuation(Serializable continuationId, FlowExecution flowExecution);
}