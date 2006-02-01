package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;

/**
 * A repository factory that returns the same (singleton) instance of a
 * {@link FlowExecutionRepository} on each invocation. Designed to be used with
 * {@link FlowExecutionRepository} implementations that are stateless and
 * therefore shareable by all threads.
 * 
 * @author Keith Donald
 */
public abstract class DelegatingFlowExecutionRepositoryFactory extends FlowExecutionRepositoryServices implements
		FlowExecutionRepositoryFactory {

	/**
	 * The repository to delegate to.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;
	
	public DelegatingFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
	}

	protected FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}
	
	protected void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}
	
	public FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}
}