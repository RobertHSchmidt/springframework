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
package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.AbstractFlowExecutionRepositoryCreator;
import org.springframework.webflow.execution.repository.support.FlowExecutionRepositoryServices;

/**
 * Creates continuation-based flow execution repositories.
 * <p>
 * All properties are optional. If a property is not set, the default values
 * will be used.
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepositoryCreator extends AbstractFlowExecutionRepositoryCreator {

	/**
	 * The flow execution continuation factory to use.
	 */
	private FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * The maximum number of continuations allowed per conversation.
	 */
	private int maxContinuations = 25;

	/**
	 * Creates a new continuation repository creator.
	 * @param repositoryServices the repository services holder
	 */
	public ContinuationFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices);
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in repositories created by this creator.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in
	 * repositories created by this creator.
	 */
	public void setMaxContinuations(int maxContinuations) {
		this.maxContinuations = maxContinuations;
	}

	public FlowExecutionRepository createRepository() {
		ContinuationFlowExecutionRepository repository = new ContinuationFlowExecutionRepository(
				getRepositoryServices());
		repository.setContinuationFactory(continuationFactory);
		repository.setMaxContinuations(maxContinuations);
		return repository;
	}

	public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository) {
		ContinuationFlowExecutionRepository impl = (ContinuationFlowExecutionRepository)repository;
		impl.setRepositoryServices(getRepositoryServices());
		impl.setContinuationFactory(continuationFactory);
		return impl;
	}
}