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
package org.springframework.webflow.config;

import java.util.Map;

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
 * @see org.springframework.webflow.config.FlowBuilder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler {

	/**
	 * The id to be assigned to the flow definition assembled by this assembler.
	 */
	private String flowId;

	/**
	 * Arbitrary properties to be assigned to the flow definition assembled by
	 * this assembler.
	 */
	private Map flowProperties;

	/**
	 * The flow builder strategy used to construct the flow from its component
	 * parts.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * Create a new flow assembler using the specified builder strategy.
	 * @param flowId the assigned flow id
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(String flowId, FlowBuilder flowBuilder) {
		setFlowId(flowId);
		setFlowBuilder(flowBuilder);
	}

	/**
	 * Create a new flow assembler using the specified builder strategy.
	 * @param flowId the assigned flow id
	 * @param flowProperties the assigned flow properties
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(String flowId, Map flowProperties, FlowBuilder flowBuilder) {
		setFlowId(flowId);
		setFlowProperties(flowProperties);
		setFlowBuilder(flowBuilder);
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
	 * Returns the id that will be assigned to the Flow built by this assembler.
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Sets the id that will be assigned to the Flow built by this assembler.
	 * @param flowId the flow id
	 */
	public void setFlowId(String flowId) {
		Assert.hasText("A unique flow definition identifier property is required for flow assembly");
		this.flowId = flowId;
	}

	/**
	 * Returns the flow properties that will be assigned to the Flow built by
	 * this assembler. The returned map is mutable.
	 */
	public Map getFlowProperties() {
		return flowProperties;
	}

	/**
	 * Sets the flow properties that will be assigned to the Flow built by this
	 * assembler.
	 * @param flowProperties the flow properties
	 */
	public void setFlowProperties(Map flowProperties) {
		this.flowProperties = flowProperties;
	}

	/**
	 * Assembles the flow, directing the construction process by delegating to
	 * the configured FlowBuilder. While the assembly process is ongoing the
	 * "assembling" flag is set to true.
	 */
	public void assembleFlow() {
		flowBuilder.init(getFlowId(), getFlowProperties());
		flowBuilder.buildStates();
		flowBuilder.buildExceptionHandlers();
		flowBuilder.buildPostProcess();
		flowBuilder.dispose();
	}
}