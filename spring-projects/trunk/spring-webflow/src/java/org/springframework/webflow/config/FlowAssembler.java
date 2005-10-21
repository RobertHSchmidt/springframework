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

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * A director for assembling flows, delegating to a <code>FlowBuilder</code>
 * builder to construct the Flow. This is the core top level class for
 * assembling a <code>Flow</code> from configuration information.
 * <p>
 * Flow assemblers, as POJOs, can also be used outside of a Spring bean factory,
 * in a standalone, programmatic fashion:
 * 
 * <pre>
 *     FlowBuilder builder = ...;
 *     Flow flow = new FlowAssembler(builder).getFlow();
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
public class FlowAssembler {

	/**
	 * The flow builder strategy used to assemble the flow produced by this
	 * assembler.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The flow assembled by this factory bean.
	 */
	private Flow flow;

	/**
	 * Create a new flow assembler using the specified builder strategy.
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(FlowBuilder flowBuilder) {
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
	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.notNull(flowBuilder, "The flow builder is required");
		this.flowBuilder = flowBuilder;
	}

	/**
	 * Does this assembler build flows with the specified FlowBuilder
	 * implementation?
	 * @param builderImplementationClass the builder implementation
	 * @return true if yes, false otherwise
	 * @throws IllegalArgumentException if specified class is not a
	 * <code>FlowBuilder</code> implementation
	 */
	public boolean buildsWith(Class builderImplementationClass) throws IllegalArgumentException {
		if (builderImplementationClass == null) {
			return false;
		}
		if (!FlowBuilder.class.isAssignableFrom(builderImplementationClass)) {
			throw new IllegalArgumentException("The flow builder implementation class '" + builderImplementationClass
					+ "' you provided to this method does not implement the '" + FlowBuilder.class.getName()
					+ "' interface");
		}
		return getFlowBuilder().getClass().equals(builderImplementationClass);
	}

	/**
	 * Returns the flow built by this factory.
	 */
	public Flow getFlow() {
		if (this.flow == null) {
			// already set the flow handle to avoid infinite loops!
			// e.g where Flow A spawns Flow B, which spawns Flow A again...
			this.flow = this.flowBuilder.init();
			this.flowBuilder.buildStates();
			this.flow = this.flowBuilder.getResult();
			this.flowBuilder.dispose();
		}
		return this.flow;
	}
}