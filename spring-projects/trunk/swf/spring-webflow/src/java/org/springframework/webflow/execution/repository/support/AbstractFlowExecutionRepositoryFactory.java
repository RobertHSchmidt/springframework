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
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryCreator;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * A convenient base for factories that create or locate flow execution
 * repositories to manage the storage of one or more flow executions
 * representing stateful user conversations.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {

	/**
	 * The creational strategy that will create FlowExecutionRepository
	 * instances as needed for management by this factory.
	 */
	private FlowExecutionRepositoryCreator repositoryCreator;

	/**
	 * Creates a new flow execution repository factory.
	 * @param repositoryCreator the creational strategy that will create
	 * FlowExecutionRepository instances as needed for management by this
	 * factory.
	 */
	protected AbstractFlowExecutionRepositoryFactory(FlowExecutionRepositoryCreator repositoryCreator) {
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

	public abstract FlowExecutionRepository getRepository(ExternalContext context);
}