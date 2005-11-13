package org.springframework.webflow.execution;

import java.io.Serializable;

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
	public FlowExecutionSerializationException(FlowExecutionStorage storageStrategy, Serializable storageId,
			FlowExecution flowExecution, String message, Throwable cause) {
		super(storageStrategy, storageId, flowExecution, message, cause);
	}
}
