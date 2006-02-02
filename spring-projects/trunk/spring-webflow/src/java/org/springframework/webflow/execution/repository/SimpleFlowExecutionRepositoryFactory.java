package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;

/**
 * This is a convenient implementation that encapsulates the assembly of what is
 * logically Spring Web Flow's default "simple" flow execution repository
 * factory.
 * <ul>
 * Specifically, "simple" means:
 * <ul>
 * <li>Uses a {@link SharedMapFlowExecutionRepositoryFactory} to manage flow
 * execution repository implementations statefully in the
 * {@link ExternalContext#getSessionMap()}, typically backed by the HTTP
 * session.
 * <li>Uses a {@link SimpleFlowExecutionRepositoryCreator} to create instances
 * of {@link SimpleFlowExecutionRepository} when requested for placement in the
 * session map.
 * </ul>
 * This class inherits from {@link FlowExecutionRepositoryServices} to allow for
 * direct configuration of services needed by the repositories created by this
 * factory.
 * 
 * @author Keith Donald
 */
public class SimpleFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	/**
	 * Creates a new simple flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions that
	 * represent conversation blueprints
	 */
	public SimpleFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SharedMapFlowExecutionRepositoryFactory(new SimpleFlowExecutionRepositoryCreator(this)));
	}

	/**
	 * A creational strategy returning {@link SimpleFlowExecutionRepository}
	 * instances.
	 * 
	 * @author Keith Donald
	 */
	private static class SimpleFlowExecutionRepositoryCreator extends AbstractFlowExecutionRepositoryCreator {
		public SimpleFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
			super(repositoryServices);
		}

		public FlowExecutionRepository createRepository() {
			return new SimpleFlowExecutionRepository(getRepositoryServices());
		}

		public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository) {
			// rehydrate the services, in case of serialization in the shared map
			((SimpleFlowExecutionRepository)repository).setRepositoryServices(getRepositoryServices());
			return repository;
		}
	}
}