package org.springframework.webflow.execution.repository;

public abstract class AbstractFlowExecutionRepositoryCreator implements FlowExecutionRepositoryCreator {

	private FlowExecutionRepositoryServices repositoryServices;

	public AbstractFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
		this.repositoryServices = repositoryServices;
	}

	protected FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	public abstract FlowExecutionRepository createRepository();

	public abstract FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository);

}