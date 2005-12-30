package org.springframework.webflow.execution;

import org.springframework.webflow.ExternalContext;

/**
 * Simple repository locator that returns the same (singleton) instance of a
 * {@link FlowExecutionRepository} on each invocation.
 * 
 * @author Keith Donald
 */
public class SingletonFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {
	private FlowExecutionRepository repository;

	public SingletonFlowExecutionRepositoryFactory(FlowExecutionRepository repository) {
		this.repository = repository;
	}

	public synchronized FlowExecutionRepository getRepository(ExternalContext context) {
		return repository;
	}
}
