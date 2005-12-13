/*
 * Copyright 2002-2005 the original author or authors.
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
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.FlowArtifactFactory;

/**
 * A flow artifact locator that obtains subflow definitions from a explict
 * {@link FlowRegistry} The remaining types of artifacts ared sourced from a
 * standard Spring BeanFactory.
 * 
 * @see FlowRegistry
 * @see FlowArtifactFactory#getSubflow(String)
 * 
 * @author Keith Donald
 */
public class FlowRegistryFlowArtifactFactory extends BeanFactoryFlowArtifactFactory {

	/**
	 * The Spring bean factory.
	 */
	private FlowRegistry subflowRegistry;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 * @param subflowLocator The locator for loading subflows
	 */
	public FlowRegistryFlowArtifactFactory(FlowRegistry subflowRegistry, BeanFactory beanFactory) {
		super(beanFactory);
		this.subflowRegistry = subflowRegistry;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		return subflowRegistry.getFlow(id);
	}
}