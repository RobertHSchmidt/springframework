/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.execution.repository.support;

import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.SharedAttributeMap;
import org.springframework.webflow.context.support.LocalSharedAttributeMap;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * Retrieves flow execution repositories from a shared, externally managed map.
 * <p>
 * The map access strategy is configurable by setting the
 * {@link #setRepositoryMapLocator(RepositoryMapLocator) sharedMapLocator}
 * property. By default the {@link SessionRepositoryMapLocator} is used which pulls in the
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
 * the {@link LocalSharedAttributeMap} to ensure thread safety.
 * 
 * @author Keith Donald
 */
public class MapFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The creational strategy that will create FlowExecutionRepository
	 * instances as needed for management by this factory.
	 */
	private FlowExecutionRepositoryCreator repositoryCreator;

	/**
	 * The map locator that returns a <code>java.util.Map</code> that allows
	 * this storage implementation to access a FlowExecutionRepository by a
	 * unique key.
	 * <p>
	 * The default is the {@link SessionRepositoryMapLocator} which returns a map backed
	 * by the {@link ExternalContext#getSessionMap}.
	 */
	private RepositoryMapLocator repositoryMapLocator = new SessionRepositoryMapLocator();

	/**
	 * Creates a new shared map repository factory.
	 * @param repositoryCreator the repository creational strategy
	 */
	public MapFlowExecutionRepositoryFactory(FlowExecutionRepositoryCreator repositoryCreator) {
		Assert.notNull(repositoryCreator, "The repository creator is required");
		this.repositoryCreator = repositoryCreator;
	}

	/**
	 * Returns the creational strategy in use that will create
	 * {@link FlowExecutionRepository} instances as needed for this factory.
	 */
	public FlowExecutionRepositoryCreator getRepositoryCreator() {
		return repositoryCreator;
	}

	/**
	 * Returns the shared, external map locator.
	 */
	public RepositoryMapLocator getRepositoryMapLocator() {
		return repositoryMapLocator;
	}

	/**
	 * Sets the shared, external map locator.
	 */
	public void setRepositoryMapLocator(RepositoryMapLocator sharedMapLocator) {
		Assert.notNull(sharedMapLocator, "The repository map locator is required");
		this.repositoryMapLocator = sharedMapLocator;
	}

	public FlowExecutionRepository getRepository(ExternalContext context) {
		SharedAttributeMap repositoryMap = repositoryMapLocator.getMap(context);
		// synchronize on the shared map's mutex for thread safety
		synchronized (repositoryMap.getMutex()) {
			String attributeName = getRepositoryAttributeName();
			FlowExecutionRepository repository = (FlowExecutionRepository)repositoryMap.get(attributeName,
					FlowExecutionRepository.class);
			if (repository == null) {
				repository = getRepositoryCreator().createRepository();
				repositoryMap.put(attributeName, repository);
			}
			else {
				getRepositoryCreator().rehydrateRepository(repository);
			}
			if (repositoryMapLocator.requiresRebindOnChange()) {
				return new RebindingFlowExecutionRepository(repository, attributeName, repositoryMap);
			}
			else {
				return repository;
			}
		}
	}

	/**
	 * Returns the shared map repository attribute key.
	 */
	protected String getRepositoryAttributeName() {
		return getRepositoryCreator().getClass().getName();
	}
}