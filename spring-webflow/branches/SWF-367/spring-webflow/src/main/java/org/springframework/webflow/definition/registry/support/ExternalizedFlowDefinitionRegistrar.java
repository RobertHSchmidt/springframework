/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.definition.registry.support;

import java.util.HashSet;
import java.util.Iterator;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * A flow definition registrar that populates a flow definition registry from flow definitions defined within
 * externalized resources. Encapsulates registration behavior common to all externalized registrars and is not tied to a
 * specific flow definition format (e.g. xml).
 * <p>
 * Concrete subclasses are expected to derive from this class to provide knowledge about a particular kind of definition
 * format by implementing the abstract template methods in this class.
 * 
 * @see org.springframework.webflow.definition.registry.FlowDefinitionRegistry
 * 
 * @author Keith Donald
 * @author Ben Hale
 */
public abstract class ExternalizedFlowDefinitionRegistrar implements FlowDefinitionRegistrar {

	private HashSet resources = new HashSet();

	/**
	 * Adds an externalized flow definition resource to be registered.
	 * @param resource the flow definition resource to be registered
	 */
	public boolean addResource(FlowDefinitionResource resource) {
		return resources.add(resource);
	}

	public void registerFlowDefinitions(FlowDefinitionRegistry registry) {
		Iterator it = resources.iterator();
		while (it.hasNext()) {
			FlowDefinitionResource resource = (FlowDefinitionResource) it.next();
			registry.registerFlowDefinition(createFlowDefinitionHolder(resource));
		}
	}

	// sub-classing hooks

	/**
	 * Template factory method subclasses must override to return the holder for the flow definition to be registered
	 * loaded from the specified resource.
	 * @param resource the externalized resource
	 * @return the flow definition holder
	 */
	protected abstract FlowDefinitionHolder createFlowDefinitionHolder(FlowDefinitionResource resource);

	public String toString() {
		return new ToStringCreator(this).append("resources", resources).toString();
	}
}