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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;

/**
 * An abstract support class that provides some assistance implementing Flow
 * registrars.
 * @author Keith Donald
 */
public abstract class FlowRegistrarSupport implements FlowRegistrar {

	/**
	 * Logger, for subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Register the flow built by the builder in the registry.
	 * @param flowId the flow identifier to be assigned (should be unique to
	 * flows in the registry)
	 * @param flowBuilder the flow builder to use to construct the flow
	 * @param registry the flow registry to register the flow in
	 */
	protected void registerFlow(String flowId, FlowBuilder flowBuilder, FlowRegistry registry) {
		registry.registerFlow(createFlowHolder(new FlowAssembler(flowId, flowBuilder)));
	}

	/**
	 * Register the flow built by the builder in the registry with the
	 * properties provided.
	 * @param flowId the flow definition identifier to be assigned (should be
	 * unique to flows in the registry)
	 * @param flowBuilder the flow builder to use to construct the flow
	 * @param registry the flow registry to register the flow in
	 * @param properties assigned flow definition properties
	 */
	protected void registerFlow(String flowId, FlowBuilder flowBuilder, FlowRegistry registry, Map properties) {
		registry.registerFlow(createFlowHolder(new FlowAssembler(flowId, flowBuilder)));
	}

	/**
	 * Factory method that returns a new default flow holder implementation.
	 * @param assembler the assembler to direct flow building
	 * @return a flow holder, to be used as a registry entry and holder for a
	 * managed flow definition.
	 */
	protected FlowHolder createFlowHolder(FlowAssembler assembler) {
		return new RefreshableFlowHolder(assembler);
	}

	public abstract void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory);

}