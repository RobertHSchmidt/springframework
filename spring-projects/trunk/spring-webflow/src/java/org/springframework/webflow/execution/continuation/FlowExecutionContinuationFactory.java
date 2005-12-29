package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionContinuationKey;

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
	public FlowExecutionContinuation createContinuation(FlowExecutionContinuationKey key, FlowExecution flowExecution);
}