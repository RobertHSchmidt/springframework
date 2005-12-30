package org.springframework.webflow.execution;

import java.util.Map;

import org.springframework.webflow.ExternalContext;

/**
 * A repository locator that accesses flow execution repositories from an
 * externally managed data map.
 * <p>
 * The map access strategy is configurable by setting the
 * {@link #setExternalMapLocator(ExternalMapLocator) externalMapLocator}
 * property. By default the {@link SessionMapLocator} is used, which pulls in
 * the {@link ExternalContext#getSessionMap()}, a map backed by a user's HTTP
 * session in a Servlet environment and a Portlet Session in a Portlet
 * environment.
 * <p>
 * When a repository lookup request is initiated, if a
 * {@link FlowExecutionRepository} is not present in the retrieved external map,
 * one will be created by having this object delegate to the configured
 * {@link FlowExecutionRepositoryCreator}. The newly created repository will
 * then be placed in the external map where it can be accessed at a later point
 * in time.
 * 
 * @author Keith Donald
 */
public class ExternalMapFlowExecutionRepositoryFactory extends AbstractFlowExecutionRepositoryFactory {

	/**
	 * The map locator that returns a <code>java.util.Map</code> that allows
	 * this storage implementation to access a FlowExecutionRepository by a
	 * unique key.
	 * <p>
	 * The default is the {@link SessionMapLocator} which returns a map backed
	 * by the {@link ExternalContext#getSessionMap}.
	 */
	private ExternalMapLocator externalMapLocator = new SessionMapLocator();

	public ExternalMapLocator getExternalMapLocator() {
		return externalMapLocator;
	}

	public void setExternalMapLocator(ExternalMapLocator externalMapLocator) {
		this.externalMapLocator = externalMapLocator;
	}

	public synchronized FlowExecutionRepository getRepository(ExternalContext context) {
		Map repositoryMap = externalMapLocator.getMap(context);
		Object repositoryKey = getRepositoryKey();
		FlowExecutionRepository repository = (FlowExecutionRepository)repositoryMap.get(repositoryKey);
		if (repository == null) {
			repository = createFlowExecutionRepository();
			repositoryMap.put(repositoryKey, repository);
		}
		return repository;
	}

	protected Object getRepositoryKey() {
		return FlowExecutionRepository.class.getName();
	}
}