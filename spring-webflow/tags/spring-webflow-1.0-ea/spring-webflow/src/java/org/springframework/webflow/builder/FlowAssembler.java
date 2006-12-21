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
package org.springframework.webflow.builder;

import org.springframework.util.Assert;

/**
 * A director for assembling flows, delegating to a {@link FlowBuilder} to
 * construct a flow. This class encapsulates the algorithm for using a
 * FlowBuilder to assemble a Flow properly. It acts as the director in the
 * classic GoF builder pattern.
 * <p>
 * Flow assemblers may be used in a standalone, programmatic fashion as follows:
 * 
 * <pre>
 *     FlowBuilder builder = ...;
 *     new FlowAssembler(&quot;myFlow&quot;, builder).assembleFlow();
 *     Flow flow = builder.getResult();
 * </pre>
 * 
 * @see org.springframework.webflow.builder.FlowBuilder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler {

	/**
	 * Parameters neccessary to assemble the flow, most notably the flow
	 * identifier.
	 */
	private FlowArtifactParameters flowParameters;

	/**
	 * The flow builder strategy used to construct the flow from its component
	 * parts.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * Create a new flow assembler that will direct Flow assembly using the
	 * specified builder strategy.
	 * @param flowId the assigned flow id
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(String flowId, FlowBuilder flowBuilder) {
		this(new FlowArtifactParameters(flowId), flowBuilder);
	}

	/**
	 * Create a new flow assembler that will direct Flow assembly using the
	 * specified builder strategy.
	 * @param flowParameters the assigned flow parameters
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(FlowArtifactParameters flowParameters, FlowBuilder flowBuilder) {
		setFlowParameters(flowParameters);
		setFlowBuilder(flowBuilder);
	}

	/**
	 * Returns the flow parameters to assign during flow assembly.
	 */
	public FlowArtifactParameters getFlowParameters() {
		return flowParameters;
	}

	/**
	 * Sets the flow parameters to assign during flow assembly.
	 * @param flowParameters flow parameters
	 */
	public void setFlowParameters(FlowArtifactParameters flowParameters) {
		this.flowParameters = flowParameters;
	}

	/**
	 * Returns the builder the factory uses to build flows.
	 */
	public FlowBuilder getFlowBuilder() {
		return flowBuilder;
	}

	/**
	 * Set the builder the factory will use to build flows.
	 */
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.notNull(flowBuilder, "The flow builder is required");
		this.flowBuilder = flowBuilder;
	}

	/**
	 * Assembles the flow, directing the construction process by delegating to
	 * the configured FlowBuilder. While the assembly process is ongoing the
	 * "assembling" flag is set to true.
	 */
	public void assembleFlow() {
		flowBuilder.init(flowParameters);
		flowBuilder.buildStates();
		flowBuilder.buildExceptionHandlers();
		flowBuilder.buildPostProcess();
		flowBuilder.dispose();
	}
}