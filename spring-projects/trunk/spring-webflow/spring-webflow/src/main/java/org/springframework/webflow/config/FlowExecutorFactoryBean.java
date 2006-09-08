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
package org.springframework.webflow.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ClientContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorFactory;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * <p>
 * The default flow executor factory implementation. Encapsulates the
 * construction and assembly of a {@link FlowExecutor}, including the provision
 * of its {@link FlowExecutionRepository} strategy.
 * </p>
 * @author Keith Donald
 */
public class FlowExecutorFactoryBean implements FlowExecutorFactory, FactoryBean {

	/**
	 * The locator the executor will use to access flow definitions registered
	 * in a central registry. Required.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * The factory the executor will use to create new flow executions.
	 */
	private FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();

	/**
	 * The strategy used by the repository to restore transient flow execution
	 * state.
	 */
	private FlowExecutionImplStateRestorer executionStateRestorer;

	/**
	 * The conversation manager to be used by the flow execution repository to
	 * begin and end new conversations driven by Spring Web Flow.
	 * <p>
	 * The default value is {@link SessionBindingConversationManager} which
	 * manages conversational state in the session map.
	 */
	private ConversationManager conversationManager = new SessionBindingConversationManager();

	/**
	 * The type of execution repository to configure with executors created by
	 * this factory.
	 */
	private RepositoryType repositoryType = RepositoryType.DEFAULT;

	/**
	 * Creates a new {@link FlowExecutor flow executor} factory.
	 * @param definitionLocator The locator the executor will use to access flow
	 * definitions registered in a central registry.
	 */
	public FlowExecutorFactoryBean(FlowDefinitionLocator definitionLocator) {
		Assert.notNull(definitionLocator, "The flow definition locator is required");
		this.definitionLocator = definitionLocator;
		this.executionStateRestorer = new FlowExecutionImplStateRestorer(definitionLocator);
	}

	/**
	 * Sets the system attributes that apply to flow executions launched by the
	 * executor created by this factory. Execution attributes may affect flow
	 * execution behavior.
	 * @param executionAttributes flow execution system attributes
	 */
	public void setExecutionAttributes(AttributeMap executionAttributes) {
		this.executionFactory.setExecutionAttributes(executionAttributes);
		this.executionStateRestorer.setExecutionAttributes(executionAttributes);
	}

	/**
	 * Convenience setter that sets a single listener that always applys to flow
	 * executions launched by the executor created by this factory.
	 * @param executionListener the flow execution listener
	 */
	public void setExecutionListener(FlowExecutionListener executionListener) {
		executionFactory.setLExecutionistener(executionListener);
		executionStateRestorer.setExecutionListener(executionListener);
	}

	/**
	 * Convenience setter that sets a list of listeners that always apply to
	 * flow executions launched by the executor created by this factory.
	 * @param executionListeners the flow execution listeners
	 */
	public void setExecutionListeners(FlowExecutionListener[] executionListeners) {
		executionFactory.setExecutionListeners(executionListeners);
		executionStateRestorer.setExecutionListeners(executionListeners);
	}

	/**
	 * Sets the strategy for loading the listeners that will observe executions
	 * of a flow definition. Allows full control over what listeners should
	 * apply to executions of a flow definition launched by the executor created
	 * by this factory.
	 */
	public void setExecutionListenerLoader(FlowExecutionListenerLoader executionListenerLoader) {
		executionFactory.setExecutionListenerLoader(executionListenerLoader);
		executionStateRestorer.setExecutionListenerLoader(executionListenerLoader);
	}

	/**
	 * Sets the type of flow execution repository that should be configured for
	 * the flow executors created by this factory. This factory encapsulates the
	 * construction of the repository implementation corresponding to the
	 * provided type.
	 * @param repositoryType the flow execution repository type
	 */
	public void setRepositoryType(RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}

	/**
	 * Sets the strategy for managing conversations that should be configured
	 * for flow executors created by this factory.
	 * <p>
	 * The conversation manager is used by the flow execution repository
	 * subsystem to begin and end new conversations that store execution state.
	 */
	public void setConversationManager(ConversationManager conversationManager) {
		this.conversationManager = conversationManager;
	}

	// implementing FlowExecutorFactory

	public FlowExecutor createFlowExecutor() {
		return new FlowExecutorImpl(definitionLocator, executionFactory,
				createExecutionRepository(executionStateRestorer));
	}

	// Template methods

	/**
	 * Factory method for creating the flow execution repository for saving and
	 * loading executing flows. Subclasses may override to customize the
	 * repository implementation used.
	 * @param executionStateRestorer the execution state restorer strategy
	 * @return the flow execution repository
	 */
	protected FlowExecutionRepository createExecutionRepository(FlowExecutionStateRestorer executionStateRestorer) {
		if (repositoryType == RepositoryType.DEFAULT) {
			return new DefaultFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.CONTINUATION) {
			return new ContinuationFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.CLIENT) {
			return new ClientContinuationFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.SINGLEKEY) {
			DefaultFlowExecutionRepository repository = new DefaultFlowExecutionRepository(executionStateRestorer,
					conversationManager);
			repository.setAlwaysGenerateNewNextKey(false);
			return repository;
		}
		else {
			throw new IllegalStateException("Cannot create execution repository - unsupported repository type "
					+ repositoryType);
		}
	}

	protected RepositoryType getRepositoryType() {
		return repositoryType;
	}

	// implementing FactoryBean

	public Class getObjectType() {
		return FlowExecutor.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return createFlowExecutor();
	}
}