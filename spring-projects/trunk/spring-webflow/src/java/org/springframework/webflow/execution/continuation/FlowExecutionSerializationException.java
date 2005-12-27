package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionStorageException;

/**
 * Root exception hierarchy for exceptions that occur during FlowExecution
 * serialization into storage.
 * @author Keith Donald
 */
public class FlowExecutionSerializationException extends FlowExecutionStorageException {

	/**
	 * Creates a new serialization exception.
	 * 
	 * @param storageStrategy the storage strategy used
	 * @param storageId the flow execution storage identifier
	 * @param flowExecution the flow execution
	 * @param message a descriptive message
	 * @param cause
	 */
	public FlowExecutionSerializationException(Serializable storageId, FlowExecution flowExecution, String message,
			Throwable cause) {
		super(storageId, flowExecution, message, cause);
	}
}