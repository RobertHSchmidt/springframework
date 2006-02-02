package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ExternalContext.SharedMap;

/**
 * Accesses flow execution repositories from a shared, externally managed map.
 * <p>
 * The map access strategy is configurable by setting the
 * {@link #setSharedMapLocator(SharedMapLocator) sharedMapLocator} property. By
 * default the {@link SessionMapLocator} is used which pulls in the
 * {@link ExternalContext#getSessionMap()}, a shared map backed by a user's
 * HTTP session in a Servlet environment and a Portlet Session in a Portlet
 * environment.
 * <p>
 * When a repository lookup request is initiated if a
 * {@link FlowExecutionRepository} is not present in the retrieved shared map,
 * one will be created by having this object delegate to the configured
 * {@link FlowExecutionRepositoryCreator}, a creational strategy. The newly
 * created repository will then be placed in the shared map where it can be
 * accessed at a later point in time. Synchronization will occur on the mutex of
 * the {@link SharedMap} to ensure thread safety.
 * 
 * @author Keith Donald
 */
public class SharedMapFlowExecutionRepositoryFactory extends AbstractFlowExecutionRepositoryFactory {

	/**
	 * The map locator that returns a <code>java.util.Map</code> that allows
	 * this storage implementation to access a FlowExecutionRepository by a
	 * unique key.
	 * <p>
	 * The default is the {@link SessionMapLocator} which returns a map backed
	 * by the {@link ExternalContext#getSessionMap}.
	 */
	private SharedMapLocator sharedMapLocator = new SessionMapLocator();

	/**
	 * Creates a new shared map repository factory.
	 * @param repositoryCreator the repository creational strategy
	 */
	public SharedMapFlowExecutionRepositoryFactory(FlowExecutionRepositoryCreator repositoryCreator) {
		super(repositoryCreator);
	}

	/**
	 * Returns the shared, external map locator.
	 */
	public SharedMapLocator getSharedMapLocator() {
		return sharedMapLocator;
	}

	/**
	 * Sets the shared, external map locator.
	 */
	public void setSharedMapLocator(SharedMapLocator sharedMapLocator) {
		this.sharedMapLocator = sharedMapLocator;
	}

	public FlowExecutionRepository getRepository(ExternalContext context) {
		SharedMap repositoryMap = sharedMapLocator.getMap(context);
		// synchronize on the shared map's mutex for thread safety
		synchronized (repositoryMap.getMutex()) {
			Object repositoryKey = getRepositoryKey();
			FlowExecutionRepository repository = (FlowExecutionRepository)repositoryMap.get(repositoryKey);
			if (repository == null) {
				repository = getRepositoryCreator().createRepository();
				repositoryMap.put(repositoryKey, repository);
			}
			else {
				getRepositoryCreator().rehydrateRepository(repository);
			}
			return repository;
		}
	}

	/**
	 * Returns the shared map repository attribute key.
	 */
	protected Object getRepositoryKey() {
		return FlowExecutionRepository.class.getName();
	}
}