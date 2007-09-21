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
package org.springframework.webflow.engine.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.engine.Flow;

/**
 * A director for assembling flows, delegating to a {@link FlowBuilder} to construct a flow. This class encapsulates the
 * algorithm for using a FlowBuilder to assemble a Flow properly. It acts as the director in the classic GoF builder
 * pattern.
 * <p>
 * Flow assemblers may be used in a standalone, programmatic fashion as follows:
 * 
 * <pre>
 *     FlowBuilder builder = ...;
 *     Flow flow = new FlowAssembler(&quot;myFlow&quot;, builder, null).assembleFlow();
 * </pre>
 * 
 * @see org.springframework.webflow.engine.builder.FlowBuilder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler {

	private static final Log logger = LogFactory.getLog(FlowAssembler.class);

	/**
	 * The unique identifier to assign to the flow.
	 */
	private String flowId;

	/**
	 * The flow builder strategy used to construct the flow from its component parts.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * Attributes that can be used to affect flow construction.
	 */
	private AttributeMap flowAttributes;

	/**
	 * Create a new flow assembler that will direct Flow assembly using the specified builder strategy.
	 * @param flowId the flow id to assign
	 * @param flowBuilder the builder the factory will use to build flows
	 * @param flowAttributes externally assigned flow attributes that can affect flow construction
	 */
	public FlowAssembler(String flowId, FlowBuilder flowBuilder, AttributeMap flowAttributes) {
		Assert.hasText(flowId, "The flow id is required for flow assembly");
		Assert.notNull(flowBuilder, "A flow builder is required for flow assembly");
		this.flowId = flowId;
		this.flowBuilder = flowBuilder;
		this.flowAttributes = (flowAttributes != null ? flowAttributes : CollectionUtils.EMPTY_ATTRIBUTE_MAP);
	}

	/**
	 * Returns the identifier to assign to the flow.
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Returns the flow builder strategy used to construct the flow from its component parts.
	 */
	public FlowBuilder getFlowBuilder() {
		return flowBuilder;
	}

	/**
	 * Returns externally assigned attributes that can be used to affect flow construction.
	 */
	public AttributeMap getFlowAttributes() {
		return flowAttributes;
	}

	/**
	 * Assembles the flow, directing the construction process by delegating to the configured FlowBuilder. Every call to
	 * this method will assemble the Flow instance.
	 * <p>
	 * This will drive the flow construction process as described in the {@link FlowBuilder} JavaDoc, starting with
	 * builder initialization using {@link FlowBuilder#init(String, AttributeMap)} and finishing by cleaning up the
	 * builder with a call to {@link FlowBuilder#dispose()}.
	 * @return the constructed flow
	 * @throws FlowBuilderException when flow assembly fails
	 */
	public Flow assembleFlow() throws FlowBuilderException {
		if (logger.isDebugEnabled()) {
			logger.debug("Assembling flow definition with id '" + flowId + "' using flow builder '" + flowBuilder
					+ "'; externally assigned flow attributes are '" + flowAttributes + "'");
		}
		try {
			flowBuilder.init(flowId, flowAttributes);
			directAssembly();
			return flowBuilder.getFlow();
		} finally {
			flowBuilder.dispose();
		}
	}

	/**
	 * Build all parts of the flow by directing flow assembly by the flow builder.
	 * @throws FlowBuilderException when flow assembly fails
	 */
	protected void directAssembly() throws FlowBuilderException {
		flowBuilder.buildVariables();
		flowBuilder.buildInputMapper();
		flowBuilder.buildStartActions();
		flowBuilder.buildInlineFlows();
		flowBuilder.buildStates();
		flowBuilder.buildGlobalTransitions();
		flowBuilder.buildEndActions();
		flowBuilder.buildOutputMapper();
		flowBuilder.buildExceptionHandlers();
	}
}