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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.RefreshableFlowDefinitionHolder;

/**
 * An abstract support class that provides some assistance implementing Flow
 * registrars that are responsible for registering one or more flow definitions
 * in a flow registry.
 * @author Keith Donald
 */
public abstract class FlowRegistrarSupport implements FlowDefinitionRegistrar {

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
	 * @param attributes assigned flow construction attributes
	 */
	protected final void registerFlow(String flowId, FlowDefinitionRegistry registry, FlowBuilder flowBuilder,
			AttributeMap attributes) {
		registry.registerFlowDefinition(createFlowHolder(new FlowAssembler(flowId, attributes, flowBuilder)));
	}

	/**
	 * Helper method to register the flow built by the builder in the registry.
	 * @param flowId the flow identifier to be assigned (should be unique to all
	 * flows in the registry)
	 * @param registry the flow registry to register the flow in
	 * @param flowBuilder the flow builder to use to construct the flow once
	 * registered
	 */
	protected final void registerFlow(String flowId, FlowDefinitionRegistry registry, FlowBuilder flowBuilder) {
		registerFlow(flowId, registry, flowBuilder, null);
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

	public abstract void registerFlowDefinitions(FlowDefinitionRegistry registry);

}