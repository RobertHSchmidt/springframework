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
package org.springframework.webflow.engine.builder.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowServiceLocator;
import org.springframework.webflow.engine.builder.RefreshableFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;

/**
 * An abstract support class that provides some assistance implementing Flow
 * registrars that are responsible for registering one or more flow definitions
 * in a flow registry.
 * @author Keith Donald
 */
public abstract class FlowRegistrarSupport implements FlowRegistrar {

	/**
	 * Logger, for subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Helper method to register the flow built by the builder in the registry.
	 * @param flowId the flow identifier to be assigned (should be unique to all
	 * flows in the registry)
	 * @param registry the flow registry to register the flow in
	 * @param flowBuilder the flow builder to use to construct the flow once
	 * registered
	 */
	protected void registerFlow(String flowId, FlowDefinitionRegistry registry, FlowBuilder flowBuilder) {
		registerFlow(flowId, registry, flowBuilder, null);
	}

	/**
	 * Helper method to register the flow built by the builder in the registry.
	 * @param flowId the flow identifier to be assigned (should be unique to all
	 * flows in the registry)
	 * @param registry the flow registry to register the flow in
	 * @param flowBuilder the flow builder to use to construct the flow once
	 * registered
	 * @param attributes assigned flow construction attributes
	 */
	protected void registerFlow(String flowId, FlowDefinitionRegistry registry, FlowBuilder flowBuilder,
			AttributeMap attributes) {
		registry.registerFlowDefinition(createFlowHolder(new FlowAssembler(flowId, attributes, flowBuilder)));
	}

	/**
	 * Helper method to register the flow built from an externalized resource in
	 * the registry.
	 * @param flowDefinition representation of the externalized flow definition
	 * resource
	 * @param registry the flow registry to register the flow in
	 * @param flowBuilder the builder that will be used to build the flow stored
	 * in the registry
	 */
	protected void registerFlow(FlowDefinitionResource flowDefinition, FlowDefinitionRegistry registry,
			FlowBuilder flowBuilder) {
		registerFlow(flowDefinition.getId(), registry, flowBuilder, flowDefinition.getAttributes());
	}

	/**
	 * Helper method to register the flow built from the XML File in the
	 * registry.
	 * @param location the resource location of the externalized flow definition
	 * @param registry the flow registry to register the flow in
	 * @param flowServiceLocator the service locator that the builder will use
	 * to wire in externally managed flow services needed by a Flow during the
	 * build process
	 */
	protected void registerXmlFlow(Resource location, FlowDefinitionRegistry registry, FlowServiceLocator flowServiceLocator) {
		registerXmlFlow(new FlowDefinitionResource(location), registry, flowServiceLocator);
	}

	/**
	 * Helper method to register the flow built from the XML File in the
	 * registry.
	 * @param flowDefinition representation of the externalized flow definition
	 * resource
	 * @param registry the flow registry to register the flow in
	 * @param flowServiceLocator the service locator that the builder will use
	 * to wire in externally managed flow services needed by a Flow during the
	 * build process
	 */
	protected void registerXmlFlow(FlowDefinitionResource flowDefinition, FlowDefinitionRegistry registry,
			FlowServiceLocator flowServiceLocator) {
		registerFlow(flowDefinition, registry, new XmlFlowBuilder(flowDefinition.getLocation(), flowServiceLocator));
	}

	/**
	 * Factory method that returns a new default flow holder implementation.
	 * @param assembler the assembler to direct flow building
	 * @return a flow holder, to be used as a registry entry and holder for a
	 * managed flow definition.
	 */
	protected FlowDefinitionHolder createFlowHolder(FlowAssembler assembler) {
		return new RefreshableFlowDefinitionHolder(assembler);
	}

	public abstract void registerFlows(FlowDefinitionRegistry registry, FlowServiceLocator flowServiceLocator);

}