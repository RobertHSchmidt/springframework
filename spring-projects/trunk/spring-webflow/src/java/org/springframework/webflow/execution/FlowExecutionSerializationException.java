package org.springframework.webflow.execution;

import java.io.Serializable;

public class FlowExecutionSerializationException extends FlowExecutionStorageException {
	public FlowExecutionSerializationException(FlowExecutionStorage storageStrategy, Serializable storageId,
			FlowExecution flowExecution, String message, Throwable cause) {
		super(storageStrategy, storageId, flowExecution, message, cause);
	}
}
