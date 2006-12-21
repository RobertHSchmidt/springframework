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
package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.State;
import org.springframework.webflow.builder.BeanInvokingActionFactory;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowServiceLocator;

/**
 * A base class for factory beans that create populated Flow Registries.
 * Subclasses should override the {@link #doPopulate(FlowRegistry)} to perform
 * the registry population logic, typically delegating to a
 * {@link FlowRegistrar} strategy.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFactoryBean implements FactoryBean, BeanFactoryAware, ResourceLoaderAware {

	/**
	 * The registry to register Flow definitions in.
	 */
	private FlowRegistryImpl flowRegistry = new FlowRegistryImpl();

	/**
	 * The locator of services needed by the Flows built for inclusion in the
	 * registry.
	 */
	private DefaultFlowServiceLocator flowServiceLocator;

	/**
	 * Sets the parent registry of the registry constructed by this factory
	 * bean.
	 * <p>
	 * A child registry will delegate to its parent if it cannot fulfill a
	 * request to locate a Flow definition.
	 * @param parent the parent flow definition registry
	 */
	public void setParent(FlowRegistry parent) {
		flowRegistry.setParent(parent);
	}

	/**
	 * Sets the factory encapsulating the creation of central Flow artifacts
	 * such as {@link Flow flows} and {@link State states}.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		flowServiceLocator.setFlowArtifactFactory(flowArtifactFactory);
	}

	/**
	 * Sets the factory for creating bean invoking actions, actions that adapt
	 * methods on objects to the {@link Action} interface.
	 */
	public void setBeanInvokingActionFactory(BeanInvokingActionFactory beanInvokingActionFactory) {
		flowServiceLocator.setBeanInvokingActionFactory(beanInvokingActionFactory);
	}

	/**
	 * Set the expression parser responsible for parsing expression strings into
	 * evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		flowServiceLocator.setExpressionParser(expressionParser);
	}

	/**
	 * Set the conversion service to use to convert between types; typically
	 * from string to a rich object type.
	 */
	public void setConversionService(ConversionService conversionService) {
		flowServiceLocator.setConversionService(conversionService);
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		flowServiceLocator.setResourceLoader(resourceLoader);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		flowServiceLocator = new DefaultFlowServiceLocator(getFlowRegistry(), beanFactory);
	}

	/**
	 * Returns the flow registry constructed by the factory bean.
	 */
	protected FlowRegistry getFlowRegistry() {
		return flowRegistry;
	}

	/**
	 * Returns the strategy for locating dependent artifacts when a Flow is
	 * being built.
	 */
	protected FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public FlowRegistry populateFlowRegistry() {
		doPopulate(getFlowRegistry());
		return getFlowRegistry();
	}

	/**
	 * Template method subclasses must override to perform registry population.
	 * @param registry the flow definition registry
	 */
	protected abstract void doPopulate(FlowRegistry registry);

	public Class getObjectType() {
		return FlowRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}
}