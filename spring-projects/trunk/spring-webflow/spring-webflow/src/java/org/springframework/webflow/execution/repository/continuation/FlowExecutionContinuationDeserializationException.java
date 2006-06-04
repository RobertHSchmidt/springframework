/**
 * 
 */
package org.springframework.webflow.execution.repository.continuation;

import org.springframework.core.NestedRuntimeException;

public class FlowExecutionContinuationDeserializationException extends NestedRuntimeException {
	public FlowExecutionContinuationDeserializationException(String message, Throwable cause) {
		super(message, cause);
	}
}