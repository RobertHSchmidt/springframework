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
import org.springframework.context.ResourceLoaderAware;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.DefaultFlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactFactory;

/**
 * A flow artifact factory that obtains subflow definitions from a explict
 * {@link FlowRegistry} The remaining types of artifacts are sourced from a
 * standard Spring {@link BeanFactory} registry.
 * 
 * @see FlowRegistry
 * @see FlowArtifactFactory#getSubflow(String)
 * 
 * @author Keith Donald
 */
public class RegistryBackedFlowArtifactFactory extends DefaultFlowArtifactFactory implements ResourceLoaderAware {

	/**
	 * The registry for locating subflows.
	 */
	private FlowRegistry subflowRegistry;

	/**
	 * The Spring bean factory that manages configured flow artifacts.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact factory that retrieves subflows from the provided
	 * registry and additional artifacts from the provided bean factory.
	 * @param beanFactory The spring bean factory
	 */
	public RegistryBackedFlowArtifactFactory(BeanFactory beanFactory) {
		this(new FlowRegistryImpl(), beanFactory);
	}
	
	/**
	 * Creates a flow artifact factory that retrieves subflows from the provided
	 * registry and additional artifacts from the provided bean factory.
	 * @param subflowRegistry The registry for loading subflows
	 * @param beanFactory The spring bean factory
	 */
	public RegistryBackedFlowArtifactFactory(FlowRegistry subflowRegistry, BeanFactory beanFactory) {
		this.subflowRegistry = subflowRegistry;
		this.beanFactory = beanFactory;
	}

	/**
	 * Returns the flow registry used by this flow artifact factory to manage
	 * subflow definitions.
	 * @return the flow registry
	 */
	public FlowRegistry getSubflowRegistry() {
		return subflowRegistry;
	}

	/**
	 * Returns the bean factory used by this flow artifact factory to manage
	 * custom flow artifacts.
	 * @return the bean factory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		return subflowRegistry.getFlow(id);
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		return beanFactory;
	}
}