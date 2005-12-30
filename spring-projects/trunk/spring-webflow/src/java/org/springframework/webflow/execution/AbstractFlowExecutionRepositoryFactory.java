package org.springframework.webflow.execution;

/**
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The factory that will create FlowExecutionRepository instances as needed
	 * for storage in the repositoryMap.
	 */
	private FlowExecutionRepositoryCreator repositoryCreator = new SimpleFlowExecutionRepositoryCreator();

	public FlowExecutionRepositoryCreator getRepositoryCreator() {
		return repositoryCreator;
	}

	public void setRepositoryCreator(FlowExecutionRepositoryCreator repositoryFactory) {
		this.repositoryCreator = repositoryFactory;
	}

	protected FlowExecutionRepository createFlowExecutionRepository() {
		return repositoryCreator.createRepository();
	}
	
	/**
	 * Trivial repository factory that simply returns a new
	 * {@link SimpleFlowExecutionRepository} on each invocation.
	 * @author Keith Donald
	 */
	public static class SimpleFlowExecutionRepositoryCreator implements FlowExecutionRepositoryCreator {
		public FlowExecutionRepository createRepository() {
			return new SimpleFlowExecutionRepository();
		}
	}
}