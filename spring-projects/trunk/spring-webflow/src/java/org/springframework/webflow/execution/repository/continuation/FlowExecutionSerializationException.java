package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.springframework.core.NestedRuntimeException;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Root exception hierarchy for exceptions that occur during FlowExecution
 * serialization into storage.
 * @author Keith Donald
 */
public class FlowExecutionSerializationException extends NestedRuntimeException {

	private Serializable continuationId;

	private FlowExecution flowExecution;

	/**
	 * Creates a new serialization exception.
	 * 
	 * @param storageStrategy the storage strategy used
	 * @param storageId the flow execution storage identifier
	 * @param flowExecution the flow execution
	 * @param message a descriptive message
	 * @param cause
	 */
	public FlowExecutionSerializationException(Serializable continuationId, FlowExecution flowExecution,
			String message, Throwable cause) {
		super(message, cause);
	}

	public Serializable getContinuationId() {
		return continuationId;
	}

	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}