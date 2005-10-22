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
 * A director for assembling flows, delegating to a <code>FlowBuilder</code>
 * builder to construct the Flow. This is the core top level class for
 * assembling a <code>Flow</code> from configuration information.
 * <p>
 * Flow assemblers, as POJOs, can also be used outside of a Spring bean factory,
 * in a standalone, programmatic fashion:
 * 
 * <pre>
 *      FlowBuilder builder = ...;
 *      Flow flow = new FlowAssembler(builder).getFlow();
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
	 * The initial id of the flow definition to support registration.
	 */
	private String flowId;
	
	/**
	 * Create a new flow assembler using the specified builder strategy.
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(FlowBuilder flowBuilder) {
		setFlowBuilder(flowBuilder);
		flowId = flowBuilder.init().getId();
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
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.notNull(flowBuilder, "The flow builder is required");
		this.flowBuilder = flowBuilder;
	}

	public synchronized String getFlowId() {
		if (flow == null) {
			return flowId;
		} else {
			return flow.getId();
		}
	}
	
	/**
	 * Returns the flow assembled by this assembler.
	 */
	public synchronized Flow getFlow() {
		if (flow == null) {
			refresh();
		}
		return flow;
	}

	public synchronized void refresh() {
		// already set the flow handle to avoid infinite loops!
		// e.g where Flow A spawns Flow B, which spawns Flow A again...
		flow = flowBuilder.init();
		flowBuilder.buildStates();
		flow = flowBuilder.getResult();
		flowBuilder.dispose();
	}
}