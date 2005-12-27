package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;

public interface FlowExecutionContinuationFactory {
	public FlowExecutionContinuation createContinuation(FlowExecutionKey key, FlowExecution flowExecution);
}