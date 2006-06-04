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

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * Convenient implementation that encapsulates the assembly of a <i>simple</i>
 * flow execution repository factory and delegates to it at runtime.
 * <ul>
 * Specifically, <i>simple</i> means this delegating repository factory:
 * <ul>
 * <li>Sets a {@link SharedMapFlowExecutionRepositoryFactory} to manage flow
 * execution repository implementations statefully in the
 * {@link ExternalContext#getSessionMap()}, typically backed by the HTTP
 * session.
 * <li>Configures it with a simple repository creator to create instances of
 * {@link SimpleFlowExecutionRepository} when requested for placement in the
 * shared session map.
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
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
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
			// rehydrate transient services in case of serialization in the
			// shared map
			((SimpleFlowExecutionRepository)repository).setRepositoryServices(getRepositoryServices());
			return repository;
		}
	}
}