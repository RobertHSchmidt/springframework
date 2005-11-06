package org.springframework.webflow.execution;

import java.io.Serializable;

public class FlowExecutionSerializationException extends FlowExecutionStorageException {
	public FlowExecutionSerializationException(Serializable storageId, FlowExecution flowExecution, String message, Throwable cause) {
		super(storageId, flowExecution, message, cause);
	}
}
