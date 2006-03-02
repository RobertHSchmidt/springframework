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
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.builder.FlowArtifactFactory;

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
	 * The flow registry to register Flow definitions in.
	 */
	private FlowRegistryImpl flowRegistry = new FlowRegistryImpl();

	/**
	 * Strategy for locating externally managed dependent artifacts when a
	 * registered Flow is being built.
	 */
	private RegistryBackedFlowArtifactFactory flowArtifactFactory;

	/**
	 * Sets the parent registry of the registry constructed by this factory
	 * bean.
	 * @param parent the parent flow definition registry
	 */
	public void setParent(FlowRegistry parent) {
		flowRegistry.setParent(parent);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		flowArtifactFactory = new RegistryBackedFlowArtifactFactory(getFlowRegistry(), beanFactory);
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		flowArtifactFactory.setResourceLoader(resourceLoader);
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
	protected FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
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