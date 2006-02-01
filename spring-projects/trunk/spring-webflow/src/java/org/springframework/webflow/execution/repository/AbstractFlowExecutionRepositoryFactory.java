package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;

/**
 * A base class for factories that create and/or locate flow execution
 * repositories that manage the store of one or more flow executions
 * representing stateful user conversations.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The factory that will create FlowExecutionRepository instances as needed
	 * for storage in the repositoryMap. The default value is a
	 * {@link SimpleFlowExecutionRepositoryCreator}.
	 */
	private FlowExecutionRepositoryCreator repositoryCreator;

	protected AbstractFlowExecutionRepositoryFactory(FlowExecutionRepositoryCreator repositoryCreator) {
		this.repositoryCreator = repositoryCreator;
	}
	
	/**
	 * Returns the repository creation strategy in use.
	 */
	public FlowExecutionRepositoryCreator getRepositoryCreator() {
		return repositoryCreator;
	}

	/**
	 * Factory method that returns a new instance of a FlowExecutionRepository.
	 * This implementation simply delegates to the configured repository creator
	 * strategy for repository creation.
	 * @return the flow execution repository
	 */
	protected FlowExecutionRepository createFlowExecutionRepository() {
		return repositoryCreator.createRepository();
	}

	public abstract FlowExecutionRepository getRepository(ExternalContext context);
}