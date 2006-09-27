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

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ClientContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;
import org.springframework.webflow.execution.repository.support.SimpleFlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * The default flow executor factory implementation. As a
 * <code>FactoryBean</code>, this class has been designed for use as a Spring
 * managed bean.
 * <p>
 * This factory encapsulates the construction and assembly of a
 * {@link FlowExecutor}, including the provision of its
 * {@link FlowExecutionRepository} strategy.
 * <p>
 * The {@link #setDefinitionLocator(FlowDefinitionLocator) definition locator}
 * property is required, all other properties are optional.
 * 
 * @author Keith Donald
 */
public class FlowExecutorFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * The locator the executor will use to access flow definitions registered
	 * in a central registry. Required.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * Execution attributes to apply.
	 */
	private MutableAttributeMap executionAttributes;
	
	/**
	 * The loader that will determine which listeners to attach to flow definition executions. 
	 */
	private FlowExecutionListenerLoader executionListenerLoader;
	
	/**
	 * The conversation manager to be used by the flow execution repository to
	 * store state associated with conversations driven by Spring Web Flow.
	 */
	private ConversationManager conversationManager = new SessionBindingConversationManager();

	/**
	 * The type of execution repository to configure with executors created by
	 * this factory.  Optional.  Will fallback to default value if not set.
	 */
	private RepositoryType repositoryType;

	/**
	 * The flow executor this factory bean creates.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Spring Web Flow executor system defaults. 
	 */
	private FlowSystemDefaults defaults = new FlowSystemDefaults();
	
	/**
	 * Sets the flow definition locator that will locate flow definitions needed
	 * for execution. Typically also a {@link FlowDefinitionRegistry}. Required.
	 * @param definitionLocator the flow definition locator (registry)
	 */
	public void setDefinitionLocator(FlowDefinitionLocator definitionLocator) {
		this.definitionLocator = definitionLocator;
	}

	/**
	 * Sets the system attributes that apply to flow executions launched by the
	 * executor created by this factory. Execution attributes may affect flow
	 * execution behavior.
	 * <p>
	 * Note: this method simply accepts a generic <code>java.util.Map</code>
	 * to allow for easy configuration by Spring. The map entries should consist
	 * of non-null String keys with object values.
	 * @param executionAttributes the flow execution system attributes
	 */
	public void setExecutionAttributes(Map executionAttributes) {
		this.executionAttributes = new LocalAttributeMap(executionAttributes);
	}

	/**
	 * Convenience setter that sets a single listener that always applies to flow
	 * executions launched by the executor created by this factory.
	 * @param executionListener the flow execution listener
	 */
	public void setExecutionListener(FlowExecutionListener executionListener) {
		setExecutionListeners(new FlowExecutionListener[] { executionListener });
	}

	/**
	 * Convenience setter that sets a list of listeners that always apply to
	 * flow executions launched by the executor created by this factory.
	 * @param executionListeners the flow execution listeners
	 */
	public void setExecutionListeners(FlowExecutionListener[] executionListeners) {
		setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));
	}

	/**
	 * Sets the strategy for loading the listeners that will observe executions
	 * of a flow definition. Allows full control over what listeners should
	 * apply to executions of a flow definition launched by the executor created
	 * by this factory.
	 */
	public void setExecutionListenerLoader(FlowExecutionListenerLoader executionListenerLoader) {
		this.executionListenerLoader = executionListenerLoader;
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

	// implementing InitializingBean

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(definitionLocator, "The flow definition locator is required");
		
		// a factory for flow executions
		FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();
		
		// a strategy to restore deserialized flow executions
		FlowExecutionImplStateRestorer executionStateRestorer = new FlowExecutionImplStateRestorer(definitionLocator);
		
		// flow execution attributes to apply
		executionAttributes = defaults.applyExecutionAttributes(executionAttributes);
		executionFactory.setExecutionAttributes(executionAttributes);
		executionStateRestorer.setExecutionAttributes(executionAttributes);
		
		// flow execution listeners to apply
		if (executionListenerLoader != null) {
			executionFactory.setExecutionListenerLoader(executionListenerLoader);
			executionStateRestorer.setExecutionListenerLoader(executionListenerLoader);
		}
		
		flowExecutor = new FlowExecutorImpl(definitionLocator, executionFactory,
				createExecutionRepository(executionStateRestorer));
	}

	// template methods

	/**
	 * Factory method for creating the flow execution repository for saving and
	 * loading executing flows. Subclasses may override to customize the
	 * repository implementation used.
	 * @param executionStateRestorer the execution state restorer strategy
	 * @return the flow execution repository
	 */
	protected FlowExecutionRepository createExecutionRepository(FlowExecutionStateRestorer executionStateRestorer) {
		repositoryType = defaults.applyIfNecessary(repositoryType);
		if (repositoryType == RepositoryType.SIMPLE) {
			return new SimpleFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.CONTINUATION) {
			return new ContinuationFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.CLIENT) {
			return new ClientContinuationFlowExecutionRepository(executionStateRestorer, conversationManager);
		}
		else if (repositoryType == RepositoryType.SINGLEKEY) {
			SimpleFlowExecutionRepository repository = new SimpleFlowExecutionRepository(executionStateRestorer,
					conversationManager);
			repository.setAlwaysGenerateNewNextKey(false);
			return repository;
		}
		else {
			throw new IllegalStateException("Cannot create execution repository - unsupported repository type "
					+ repositoryType);
		}
	}

	/**
	 * Returns the type of flow execution repository created by this
	 * factory.
	 */
	protected RepositoryType getRepositoryType() {
		return repositoryType;
	}

	/**
	 * Returns the strategy for managing conversations that should be configured
	 * for flow executors created by this factory.
	 */
	protected ConversationManager getConversationManager() {
		return conversationManager;
	}

	// implementing FactoryBean

	public Class getObjectType() {
		return FlowExecutor.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return flowExecutor;
	}
}