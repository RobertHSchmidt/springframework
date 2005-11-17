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
package org.springframework.webflow.config.registry;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.ResourceHolder;

/**
 * A director for assembling flows, delegating to a {@link FlowBuilder} to
 * construct a flow. This class encapsulates the algorithm for using a
 * FlowBuilder to assemble a Flow properly.
 * <p>
 * An instance of this class is also a {@link FlowDefinitionHolder} to support
 * registration in a refreshable {@link FlowRegistry}.
 * <p>
 * Flow assemblers may be used in a standalone, programmatic fashion as follows:
 * 
 * <pre>
 *       FlowBuilder builder = ...;
 *       Flow flow = new FlowAssembler("myFlow", builder).getFlow();
 * </pre>
 * 
 * @see FlowBuilder
 * @see FlowDefinitionHolder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler implements FlowDefinitionHolder {

	/**
	 * The id of the flow definition to be assembled. 
	 */
	private String flowId;

	/**
	 * Additional properties about the flow definition to be assembled.
	 */
	private Map flowProperties;
	
	/**
	 * The flow builder strategy used to assemble the flow produced by this
	 * assembler.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The flow definition assembled by this assembler.
	 */
	private Flow flow;

	/**
	 * A flag indicating whether or not this assembler is in the middle of the
	 * assembly process.
	 */
	private boolean assembling;

	/**
	 * A last modified date for the backing flow resource.
	 */
	private long lastModified;

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
	protected FlowBuilder getFlowBuilder() {
		return this.flowBuilder;
	}

	/**
	 * Set the builder the factory will use to build flows.
	 */
	protected void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.notNull(flowBuilder, "The flow builder is required");
		this.flowBuilder = flowBuilder;
	}

	public String getId() {
		return getFlowId();
	}

	protected String getFlowId() {
		return flowId;
	}
	
	protected void setFlowId(String flowId) {
		Assert.hasText("The flow definition identifier property is required for assembly");
		this.flowId = flowId;
	}
	
	public Map getFlowProperties() {
		return flowProperties;
	}

	public void setFlowProperties(Map flowProperties) {
		this.flowProperties = flowProperties;
	}

	protected boolean isAssembled() {
		if (flow == null || flow.getStateCount() == 0) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Returns the flow assembled by this assembler.
	 */
	public synchronized Flow getFlow() {
		if (assembling) {
			// we're in the middle of assembling, just return
			return flow;
		}
		if (!isAssembled()) {
			assembleFlow();
		}
		else {
			refreshIfChanged();
		}
		return flow;
	}

	protected void assembleFlow() {
		try {
			// set the assembling flag before building states to avoid infinite
			// loops! This would happen, for example, where Flow A spawns Flow B
			// as a subflow which spawns Flow A again (recursively)...
			assembling = true;
			this.lastModified = getLastModified();
			flow = flowBuilder.init(getFlowId(), getFlowProperties());
			flowBuilder.buildStates();
			flowBuilder.buildExceptionHandlers();
			flow = flowBuilder.getResult();
			flowBuilder.dispose();
		}
		finally {
			assembling = false;
		}
	}

	/**
	 * Reassemble the flow if its underlying resource has changed.
	 */
	protected void refreshIfChanged() {
		if (this.lastModified == -1) {
			return;
		}
		long lastModified = getLastModified();
		if (this.lastModified != lastModified) {
			this.lastModified = lastModified;
			refresh();
		}
	}

	private long getLastModified() {
		if (getFlowBuilder() instanceof ResourceHolder) {
			Resource resource = ((ResourceHolder)getFlowBuilder()).getResource();
			try {
				return resource.getFile().lastModified();
			}
			catch (IOException e) {

			}
			catch (SecurityException e) {

			}
		}
		return -1;
	}

	public synchronized void refresh() {
		assembleFlow();
	}
}