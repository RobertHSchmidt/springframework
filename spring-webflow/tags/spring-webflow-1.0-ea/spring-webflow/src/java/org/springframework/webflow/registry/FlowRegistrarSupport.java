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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactParameters;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.XmlFlowBuilder;

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
	 * @param flowBuilder the flow builder to use to construct the flow
	 * @param registry the flow registry to register the flow in
	 */
	protected void registerFlow(String flowId, FlowBuilder flowBuilder, FlowRegistry registry) {
		registry.registerFlow(createFlowHolder(new FlowAssembler(flowId, flowBuilder)));
	}

	/**
	 * Helper method to register the flow built by the builder in the registry
	 * with the properties provided.
	 * @param flowParameters the flow definition parameters to be assigned
	 * (allows full control over what flow properties get assigned)
	 * @param flowBuilder the flow builder to use to construct the flow
	 * @param registry the flow registry to register the flow in
	 */
	protected void registerFlow(FlowArtifactParameters flowParameters, FlowBuilder flowBuilder, FlowRegistry registry) {
		registry.registerFlow(createFlowHolder(new FlowAssembler(flowParameters, flowBuilder)));
	}

	/**
	 * Helper method to register the flow built from the XML File in the
	 * registry.
	 * @param flowId the flow identifier to be assigned (should be unique to all
	 * flows in the registry)
	 * @param location the location of the XML-based flow definition resource
	 * @param flowArtifactFactory the flow artifact factory that the builder
	 * will use to wire in externally managed flow artifacts during the build
	 * process
	 * @param registry the flow registry to register the flow in
	 */
	protected void registerXmlFlow(String flowId, Resource location, FlowArtifactFactory flowArtifactFactory,
			FlowRegistry registry) {
		registerFlow(new FlowArtifactParameters(flowId), new XmlFlowBuilder(location, flowArtifactFactory), registry);
	}

	/**
	 * Helper method to register the flow built from the XML File in the
	 * registry.
	 * @param flowParameters the flow definition parameters to be assigned
	 * (allows full control over what flow properties get assigned)
	 * @param location the location of the XML-based flow definition resource
	 * @param flowArtifactFactory the flow artifact factory that the builder
	 * will use to wire in externally managed flow artifacts during the build
	 * process
	 * @param registry the flow registry to register the flow in
	 */
	protected void registerXmlFlow(FlowArtifactParameters flowParameters, Resource location,
			FlowArtifactFactory flowArtifactFactory, FlowRegistry registry) {
		registerFlow(flowParameters, new XmlFlowBuilder(location, flowArtifactFactory), registry);
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