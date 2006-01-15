package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * Root exception hierarchy for exceptions that occur during FlowExecution
 * serialization into storage.
 * 
 * @author Keith Donald
 */
public class FlowExecutionSerializationException extends FlowExecutionRepositoryException {

	/**
	 * The id of the continuation whose flow execution could not be serialized.
	 */
	private Serializable continuationId;

	/**
	 * The flow execution that could not be serialized.
	 */
	private FlowExecution flowExecution;

	/**
	 * Creates a new serialization exception.
	 * @param continuationId the continuation id
	 * @param flowExecution the flow execution
	 * @param message a descriptive message
	 * @param cause
	 */
	public FlowExecutionSerializationException(Serializable continuationId, FlowExecution flowExecution,
			String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Returns the flow execution continuation id.
	 */
	public Serializable getContinuationId() {
		return continuationId;
	}

	/**
	 * Returns the flow execution that could not be serialized.
	 */
	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}