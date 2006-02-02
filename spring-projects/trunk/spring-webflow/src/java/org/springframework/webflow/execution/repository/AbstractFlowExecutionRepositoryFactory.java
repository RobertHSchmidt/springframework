package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;

/**
 * A convenient base for factories that create or locate flow execution
 * repositories to manage the storage of one or more flow executions
 * representing stateful user conversations.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The creational strategy that will create FlowExecutionRepository
	 * instances as needed for management by this factory.
	 */
	private FlowExecutionRepositoryCreator repositoryCreator;

	/**
	 * Creates a new flow execution repository factory.
	 * @param repositoryCreator the creational strategy that will create
	 * FlowExecutionRepository instances as needed for management by this
	 * factory.
	 */
	protected AbstractFlowExecutionRepositoryFactory(FlowExecutionRepositoryCreator repositoryCreator) {
		this.repositoryCreator = repositoryCreator;
	}

	/**
	 * Returns the creational strategy in use that will create
	 * {@link FlowExecutionRepository} instances as needed for this factory.
	 */
	public FlowExecutionRepositoryCreator getRepositoryCreator() {
		return repositoryCreator;
	}

	public abstract FlowExecutionRepository getRepository(ExternalContext context);
}