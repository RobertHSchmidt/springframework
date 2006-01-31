package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;

/**
 * A repository factory that returns the same (singleton) instance of a
 * {@link FlowExecutionRepository} on each invocation. Designed to be used with
 * {@link FlowExecutionRepository} implementations that are stateless and
 * therefore shareable by all threads.
 * 
 * @author Keith Donald
 */
public class SingletonFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The singleton repository.
	 */
	private FlowExecutionRepository repository;

	/**
	 * Creates a new singleton flow execution repository that simply returns the
	 * repository provided everytime.
	 * @param repository the singleton repository
	 */
	public SingletonFlowExecutionRepositoryFactory(FlowExecutionRepository repository) {
		this.repository = repository;
	}

	public FlowExecutionRepository getRepository(ExternalContext context) {
		return repository;
	}
}