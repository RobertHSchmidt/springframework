package org.springframework.webflow.execution.repository;

import org.springframework.webflow.execution.FlowLocator;

/**
 * A repository factory that returns the same (singleton) instance of a
 * {@link FlowExecutionRepository} on each invocation. Designed to be used with
 * {@link FlowExecutionRepository} implementations that are stateless and
 * therefore shareable by all threads.
 * 
 * @author Keith Donald
 */
public class SimpleFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	public SimpleFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SharedMapFlowExecutionRepositoryFactory(new SimpleFlowExecutionRepositoryCreator(this)));
	}

	private static class SimpleFlowExecutionRepositoryCreator extends AbstractFlowExecutionRepositoryCreator {
		public SimpleFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
			super(repositoryServices);
		}

		public FlowExecutionRepository createRepository() {
			return new SimpleFlowExecutionRepository(getRepositoryServices());
		}

		public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository) {
			((SimpleFlowExecutionRepository)repository).setRepositoryServices(getRepositoryServices());
			return repository;
		}
	}
}