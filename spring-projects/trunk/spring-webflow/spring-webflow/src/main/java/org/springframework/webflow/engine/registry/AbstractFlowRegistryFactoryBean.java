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
package org.springframework.webflow.engine.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.bean.BeanInvokingActionFactory;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.builder.FlowArtifactFactory;
import org.springframework.webflow.engine.builder.FlowServiceLocator;
import org.springframework.webflow.execution.Action;

/**
 * A base class for factory beans that create populated Flow Registries.
 * Subclasses should override the {@link #doPopulate(FlowDefinitionRegistry)} to
 * perform the registry population logic, typically delegating to a
 * {@link FlowDefinitionRegistrar} strategy.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFactoryBean implements FactoryBean, BeanFactoryAware, ResourceLoaderAware,
		InitializingBean {

	/**
	 * The registry to register Flow definitions in.
	 */
	private FlowDefinitionRegistryImpl flowRegistry = new FlowDefinitionRegistryImpl();

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
	 * Sets the parent registry of the registry constructed by this factory
	 * bean.
	 * <p>
	 * A child registry will delegate to its parent if it cannot fulfill a
	 * request to locate a Flow definition.
	 * @param parent the parent flow definition registry
	 */
	public void setParent(FlowDefinitionRegistry parent) {
		flowRegistry.setParent(parent);
	}

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

	public Class getObjectType() {
		return FlowDefinitionRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() {
		flowServiceLocator = createFlowServiceLocator();
		initRegistryFactoryBean();
	}

	/**
	 * Called after properties set, subclasses may override to do custom
	 * initialization.
	 */
	protected void initRegistryFactoryBean() {

	}

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
		return new DefaultFlowServiceLocator(flowRegistry, beanFactory);
	}

	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public FlowDefinitionRegistry populateFlowRegistry() {
		doPopulate(getFlowRegistry());
		return getFlowRegistry();
	}

	/**
	 * Template method subclasses must override to perform registry population.
	 * @param registry the flow definition registry
	 */
	protected abstract void doPopulate(FlowDefinitionRegistry registry);

	/**
	 * Returns the strategy for locating dependent artifacts when a Flow is
	 * being built.
	 */
	protected FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	/**
	 * Returns the flow registry constructed by the factory bean.
	 */
	protected FlowDefinitionRegistry getFlowRegistry() {
		return flowRegistry;
	}
}