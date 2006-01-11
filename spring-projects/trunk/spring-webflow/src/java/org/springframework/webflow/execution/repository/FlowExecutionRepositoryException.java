package org.springframework.webflow.execution.repository;

import org.springframework.webflow.FlowException;

public abstract class FlowExecutionRepositoryException extends FlowException {
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
