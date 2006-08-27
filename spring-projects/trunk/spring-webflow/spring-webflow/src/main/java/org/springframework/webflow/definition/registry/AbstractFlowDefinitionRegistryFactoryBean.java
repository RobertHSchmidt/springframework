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
package org.springframework.webflow.definition.registry;

import org.springframework.beans.factory.FactoryBean;

/**
 * A base class for factory beans that create populated Flow Registries.
 * Subclasses should override the {@link #doPopulate(FlowDefinitionRegistry)} to
 * perform the registry population logic, typically delegating to a
 * {@link FlowDefinitionRegistrar} strategy to perform the population.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowDefinitionRegistryFactoryBean implements FactoryBean {

	/**
	 * The registry to register Flow definitions in.
	 */
	private FlowDefinitionRegistryImpl flowRegistry = new FlowDefinitionRegistryImpl();

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

	// implementing factory bean
	
	public Class getObjectType() {
		return FlowDefinitionRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public final FlowDefinitionRegistry populateFlowRegistry() {
		doPopulate(getFlowRegistry());
		return getFlowRegistry();
	}

	// subclassing hooks
	
	/**
	 * Template method subclasses must override to perform registry population.
	 * @param registry the flow definition registry
	 */
	protected abstract void doPopulate(FlowDefinitionRegistry registry);

	/**
	 * Returns the flow registry constructed by the factory bean.
	 */
	protected FlowDefinitionRegistry getFlowRegistry() {
		return flowRegistry;
	}
}