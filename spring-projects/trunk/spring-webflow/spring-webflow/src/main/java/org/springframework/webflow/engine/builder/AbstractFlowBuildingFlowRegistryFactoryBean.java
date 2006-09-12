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
package org.springframework.webflow.engine.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.bean.BeanInvokingActionFactory;
import org.springframework.webflow.definition.registry.AbstractFlowDefinitionRegistryFactoryBean;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.execution.Action;

/**
 * <p>
 * A base class for factory beans that created populated registries of flow
 * definitions built using a {@link FlowBuilder}.
 * </p>
 * <p>
 * Subclasses should override the {@link #doPopulate(FlowDefinitionRegistry)} to
 * perform the registry population logic, typically delegating to a
 * {@link FlowDefinitionRegistrar} strategy.
 * </p>
 * @author Keith Donald
 */
public abstract class AbstractFlowBuildingFlowRegistryFactoryBean extends AbstractFlowDefinitionRegistryFactoryBean
		implements BeanFactoryAware, ResourceLoaderAware {

	/**
	 * The locator of services needed by the Flows built for inclusion in the
	 * registry.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * The factory encapsulating the creation of central Flow artifacts such as
	 * {@link Flow flows} and {@link State states}.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * The factory encapsulating the creation of bean invoking actions, actions
	 * that adapt methods on objects to the {@link Action} interface.
	 */
	private BeanInvokingActionFactory beanInvokingActionFactory;

	/**
	 * The parser for parsing expression strings into evaluatable expression
	 * objects.
	 */
	private ExpressionParser expressionParser;

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService;

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader;

	/**
	 * The Spring bean factory that manages configured flow artifacts.
	 */
	private BeanFactory beanFactory;

	/**
	 * Sets the factory encapsulating the creation of central Flow artifacts
	 * such as {@link Flow flows} and {@link State states}.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		this.flowArtifactFactory = flowArtifactFactory;
	}

	/**
	 * Sets the factory for creating bean invoking actions, actions that adapt
	 * methods on objects to the {@link Action} interface.
	 */
	public void setBeanInvokingActionFactory(BeanInvokingActionFactory beanInvokingActionFactory) {
		this.beanInvokingActionFactory = beanInvokingActionFactory;
	}

	/**
	 * Set the expression parser responsible for parsing expression strings into
	 * evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	/**
	 * Set the conversion service to use to convert between types; typically
	 * from string to a rich object type.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected final void init() {
		flowServiceLocator = createFlowServiceLocator();
		init(flowServiceLocator);
	}

	// subclassing hooks

	/**
	 * Factory method for creating the service locator used to locate webflow
	 * services during flow assembly. Subclasses may override to customize the
	 * configuration of the locator returned.
	 * @return the service locator
	 */
	protected FlowServiceLocator createFlowServiceLocator() {
		DefaultFlowServiceLocator serviceLocator = newDefaultFlowServiceLocator();
		if (flowArtifactFactory != null) {
			serviceLocator.setFlowArtifactFactory(flowArtifactFactory);
		}
		if (beanInvokingActionFactory != null) {
			serviceLocator.setBeanInvokingActionFactory(beanInvokingActionFactory);
		}
		if (expressionParser != null) {
			serviceLocator.setExpressionParser(expressionParser);
		}
		if (conversionService != null) {
			serviceLocator.setConversionService(conversionService);
		}
		if (resourceLoader != null) {
			serviceLocator.setResourceLoader(resourceLoader);
		}
		return serviceLocator;
	}

	/**
	 * Template method for creating the default service locator used to locate
	 * webflow services during flow assembly. Subclasses may override to
	 * customize the implementation of the default locator returned.
	 * @return the default service locator
	 */
	protected DefaultFlowServiceLocator newDefaultFlowServiceLocator() {
		return new DefaultFlowServiceLocator(getRegistry(), beanFactory);
	}

	/**
	 * Called after properties set but before registry population. Subclasses
	 * may override to perform custom initialization.
	 * @param the flow service locator to use to locate externally managed
	 * services needed duringf flow building and assembly.
	 */
	protected void init(FlowServiceLocator flowServiceLocator) {

	}

	protected abstract void doPopulate(FlowDefinitionRegistry registry);
}