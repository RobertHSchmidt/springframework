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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.engine.builder.FlowBuilder;

/**
 * A flow registrar that populates a flow registry from flow definitions defined
 * within externalized resources. Encapsulates registration behaivior common to
 * all externalized registrars and is not tied to a specific flow definition
 * format (e.g. xml).
 * <p>
 * Concrete subclasses are expected to derive from this class to provide
 * knowledge about a particular kind of definition format by implementing the
 * abstract template methods in this class.
 * <p>
 * By default, when configuring the <code>flowLocations</code> property, flow
 * definition registered by this registrar will be assigned a registry
 * identifier equal to the filename of the underlying definition resource, minus
 * the filename extension. For example, a XML-based flow definition defined in
 * the file "flow1.xml" will be identified as "flow1" when registered in a
 * registry.
 * <p>
 * For full control over the assignment of flow identifiers and flow properties,
 * configure formal
 * {@link org.springframework.webflow.definition.registry.FlowDefinitionResource}
 * instances using the <code>flowDefinitions</code> property.
 * 
 * @see org.springframework.webflow.definition.registry.FlowDefinitionResource
 * @see org.springframework.webflow.definition.registry.FlowDefinitionRegistry
 * @see org.springframework.webflow.engine.builder.FlowServiceLocator
 * @see org.springframework.webflow.engine.builder.FlowBuilder
 * 
 * @author Keith Donald
 */
public abstract class ExternalizedFlowRegistrar extends FlowRegistrarSupport {

	/**
	 * File locations of externalized flow definition resources to load.
	 */
	private Set flowLocations = new HashSet();

	/**
	 * A set of formal externalized flow definitions to load.
	 * @see {@link FlowDefinitionResource}
	 */
	private Set flowDefinitions = new HashSet();
	
	/**
	 * Sets the locations (file paths) pointing to externalized flow
	 * definitions.
	 * <p>
	 * Flows registered from this set will be automatically assigned an id based
	 * on the filename of the flow resource.
	 * @param flowLocations the resource locations
	 * @see #getFlowId(Resource)
	 */
	public void setFlowLocations(Resource[] flowLocations) {
		this.flowLocations = new HashSet(Arrays.asList(flowLocations));
	}

	/**
	 * Sets the formal set of externalized flow definitions this registrar will
	 * register.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinitions the externalized flow definition specification
	 */
	public void setFlowDefinitions(FlowDefinitionResource[] flowDefinitions) {
		this.flowDefinitions = new HashSet(Arrays.asList(flowDefinitions));
	}

	/**
	 * Adds a flow location pointing to an externalized flow resource.
	 * <p>
	 * The flow registered from this location will automatically assigned an id
	 * based on the filename of the flow resource.
	 * @param flowLocation the definition location
	 * @see #getFlowId(Resource)
	 */
	public boolean addFlowLocation(Resource flowLocation) {
		return flowLocations.add(flowLocation);
	}

	/**
	 * Adds the flow locations pointing to externalized flow resources.
	 * <p>
	 * The flow registered from this location will automatically assigned an id
	 * based on the filename of the flow resource.
	 * @param flowLocations the definition locations
	 * @see #getFlowId(Resource)
	 */
	public boolean addFlowLocations(Resource[] flowLocations) {
		if (flowLocations == null) {
			return false;
		}
		return this.flowLocations.addAll(Arrays.asList(flowLocations));
	}

	/**
	 * Adds an externalized flow definition specification pointing to an
	 * externalized flow resource.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinition the definition the definition resource
	 */
	public boolean addFlowDefinition(FlowDefinitionResource flowDefinition) {
		return flowDefinitions.add(flowDefinition);
	}

	/**
	 * Adds the externalzied flow definitions pointing to externalized flow
	 * resources.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinition the definitions
	 */
	public boolean addFlowDefinitions(FlowDefinitionResource[] flowDefinition) {
		if (flowDefinition == null) {
			return false;
		}
		return this.flowDefinitions.addAll(Arrays.asList(flowDefinition));
	}

	public void registerFlows(FlowDefinitionRegistry registry) {
		registerFlowLocations(registry);
		registerFlowDefinitions(registry);
	}

	/**
	 * Register the Flow definitions at the configured file locations
	 * @param registry the registry
	 * @param flowServiceLocator the flow artifactFactory
	 */
	private void registerFlowLocations(FlowDefinitionRegistry registry) {
		Iterator it = flowLocations.iterator();
		while (it.hasNext()) {
			Resource location = (Resource)it.next();
			if (isFlowDefinitionResource(location)) {
				FlowDefinitionResource resource = createFlowDefinitionResource(location);
				FlowBuilder builder = createFlowBuilder(resource.getLocation());
				registerFlow(resource, registry, builder);
			}
		}
	}

	/**
	 * Register the Flow definitions at the configured file locations
	 * @param registry the registry
	 * @param flowServiceLocator the flow artifactFactory
	 */
	private void registerFlowDefinitions(FlowDefinitionRegistry registry) {
		Iterator it = flowDefinitions.iterator();
		while (it.hasNext()) {
			FlowDefinitionResource resource = (FlowDefinitionResource)it.next();
			FlowBuilder builder = createFlowBuilder(resource.getLocation());
			registerFlow(resource, registry, builder);
		}
	}

	/**
	 * Helper method to register the flow built from an externalized resource in
	 * the registry.
	 * @param resource representation of the externalized flow definition
	 * resource
	 * @param registry the flow registry to register the flow in
	 * @param flowBuilder the builder that will be used to build the flow stored
	 * in the registry
	 */
	protected final void registerFlow(FlowDefinitionResource resource, FlowDefinitionRegistry registry,
			FlowBuilder flowBuilder) {
		registerFlow(resource.getId(), registry, flowBuilder, resource.getAttributes());
	}
	
	/**
	 * Template method that calculates if the given file resource is actually a
	 * flow definition resource. Resources that aren't flow definitions will be
	 * ignored. Subclasses may override; this implementation simply returns true.
	 * @param file the file
	 * @return true if yes, false otherwise
	 */
	protected boolean isFlowDefinitionResource(Resource resource) {
		return true;
	}

	/**
	 * Factory method that creates a flow definition from an externalized resource location.
	 * @param location the location of the resource
	 * @return the externalized flow definition pointer
	 */
	protected FlowDefinitionResource createFlowDefinitionResource(Resource location) {
		return new FlowDefinitionResource(location);
	}
	
	/**
	 * Factory method that returns a new externalized flow builder that will
	 * construct the registered Flow. Subclasses must override.
	 * @param location the externalized flow definition location
	 * @param flowServiceLocator the flow artifact factory
	 * @return the flow builder
	 */
	protected abstract FlowBuilder createFlowBuilder(Resource location);

	public String toString() {
		return new ToStringCreator(this).append("flowLocations", flowLocations).append("flowDefinitions",
				flowDefinitions).toString();
	}
}