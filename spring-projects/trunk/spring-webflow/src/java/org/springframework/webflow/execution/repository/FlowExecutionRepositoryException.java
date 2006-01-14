package org.springframework.webflow.execution.repository;

import org.springframework.webflow.FlowException;

/**
 * The root class for exceptions thrown by {@link FlowExecutionRepository}
 * objects.
 * @author Keith Donald
 */
public abstract class FlowExecutionRepositoryException extends FlowException {
	
	/**
	 * The repository that threw this exception. 
	 */
	private transient FlowExecutionRepository repository;

	/**
	 * Creates a new flow execution repository exception.
	 * @param repository the repository that had a problem
	 * @param message the message
	 */
	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message) {
		super(message);
		this.repository = repository;
	}

	/**
	 * Creates a new flow execution repository exception.
	 * @param repository the repository that had a problem
	 * @param message the message
	 * @param cause the root cause of the problem
	 */
	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message, Throwable cause) {
		super(message, cause);
		this.repository = repository;
	}

	/**
	 * Returns the repository.
	 */
	public FlowExecutionRepository getRepository() {
		return repository;
	}
}