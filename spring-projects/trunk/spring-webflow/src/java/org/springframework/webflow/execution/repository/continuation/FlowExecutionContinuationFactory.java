package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A factory for creating different {@link FlowExecutionContinuation}
 * implementations.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionContinuationFactory {

	/**
	 * Creates a new flow execution continuation.
	 * @param continuationId the assigned continuation id
	 * @param flowExecution the flow execution
	 * @return the continuation
	 */
	public FlowExecutionContinuation createContinuation(Serializable continuationId, FlowExecution flowExecution);
}