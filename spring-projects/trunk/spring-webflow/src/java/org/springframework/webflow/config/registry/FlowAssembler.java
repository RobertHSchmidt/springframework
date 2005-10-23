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

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowBuilder;

/**
 * A director for assembling flows, delegating to a {@link FlowBuilder} builder
 * to construct a flow. This is the core top level class for assembling a
 * {@link Flow} definition from configuration information.
 * <p>
 * An instance of this class is also a {@link FlowDefinitionHolder} to support
 * registration in a refreshable {@link FlowRegistry}.
 * <p>
 * Flow assemblers may be used in a standalone, programmatic fashion as follows:
 * 
 * <pre>
 *       FlowBuilder builder = ...;
 *       Flow flow = new FlowAssembler(builder).getFlow();
 * </pre>
 * 
 * <p>
 * <b>Exposed configuration properties:</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowBuilder</td>
 * <td><i>null</i></td>
 * <td>Set the builder the factory will use to build flows. This is a required
 * property.</td>
 * </tr>
 * </table>
 * 
 * @see FlowBuilder
 * @see FlowDefinitionHolder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler implements FlowDefinitionHolder {

	/**
	 * The flow builder strategy used to assemble the flow produced by this
	 * assembler.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The flow definition assembled by this factory bean.
	 */
	private Flow flow;

	/**
	 * A flag indicating if the flow has been assembled.
	 */
	private boolean assembled;

	/**
	 * Create a new flow assembler using the specified builder strategy.
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(FlowBuilder flowBuilder) {
		setFlowBuilder(flowBuilder);
		flow = flowBuilder.init();
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

	public synchronized boolean isAssembled() {
		return assembled;
	}

	public String getId() {
		return flow.getId();
	}

	/**
	 * Returns the flow assembled by this assembler.
	 */
	public synchronized Flow getFlow() {
		if (!assembled) {
			// set the assembled flag before building states to avoid infinite
			// loops! This would happen, for example, where Flow A spawns Flow B
			// as a subflow which spawns Flow A again (recursively)...
			assembled = true;
			flowBuilder.buildStates();
			flow = flowBuilder.getResult();
			flowBuilder.dispose();
		}
		return flow;
	}

	public synchronized void refresh() {
		flow = flowBuilder.init();
		flowBuilder.buildStates();
		flow = flowBuilder.getResult();
		flowBuilder.dispose();
	}
}