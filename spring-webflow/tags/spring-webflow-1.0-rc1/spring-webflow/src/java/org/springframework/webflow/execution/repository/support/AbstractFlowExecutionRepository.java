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

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * A convenient base for flow execution repository implementations.
 * <p>
 * Exposes a configuration interface for setting the set of services common to
 * most repository implementations. Also provides some basic implementation
 * assistance.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepository implements FlowExecutionRepository {

	/**
	 * A holder for the services needed by this repository.
	 */
	private transient FlowExecutionRepositoryServices repositoryServices;

	/**
	 * No-arg constructor to satisfy use with subclass implementations are that serializable.
	 */
	protected AbstractFlowExecutionRepository() {
		
	}
	
	/**
	 * Creates a new flow execution repository
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public AbstractFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		setRepositoryServices(repositoryServices);
	}

	/**
	 * Returns the holder for accessing common services needed by this
	 * repository.
	 */
	public FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	/**
	 * Sets the holder for accessing common services needed by this repository.
	 */
	public void setRepositoryServices(FlowExecutionRepositoryServices repositoryServices) {
		Assert.notNull(repositoryServices, "The repository services instance is required");
		this.repositoryServices = repositoryServices;
	}

	public FlowExecution createFlowExecution(String flowId) {
		Flow flow = repositoryServices.getFlowLocator().getFlow(flowId);
		return new FlowExecutionImpl(flow, repositoryServices.getListenerLoader().getListeners(flow));
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) {
		return new FlowExecutionKey(generateId(), generateId());
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution, Serializable conversationId) {
		return new FlowExecutionKey(conversationId, generateId());
	}

	protected FlowExecution rehydrate(FlowExecution flowExecution) {
		((FlowExecutionImpl)flowExecution).rehydrate(repositoryServices.getFlowLocator(), repositoryServices
				.getListenerLoader());
		return flowExecution;
	}

	/**
	 * Helper to generate a new unique object identifier using the configured
	 * {@link FlowExecutionRepositoryServices}.
	 * @return the generated uid
	 */
	protected Serializable generateId() {
		return repositoryServices.getUidGenerator().generateId();
	}
}