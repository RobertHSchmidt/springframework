package org.springframework.webflow.execution;

import java.io.Serializable;

public interface FlowExecutionRepository {
	public FlowExecutionKey generateKey() throws FlowExecutionStorageException;

	public FlowExecutionKey generateContinuationKey(Serializable conversationId) throws FlowExecutionStorageException;

	public FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionStorageException;

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionStorageException;

	public void remove(FlowExecutionKey key) throws FlowExecutionStorageException;
}