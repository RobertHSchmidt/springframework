package org.springframework.webflow.execution;

import org.springframework.core.NestedRuntimeException;

public abstract class FlowExecutionRepositoryException extends NestedRuntimeException {
	private transient FlowExecutionRepository repository;

	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message) {
		super(message);
		this.repository = repository;
	}

	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message, Throwable cause) {
		super(message, cause);
		this.repository = repository;
	}

	public FlowExecutionRepository getRepository() {
		return repository;
	}
}
