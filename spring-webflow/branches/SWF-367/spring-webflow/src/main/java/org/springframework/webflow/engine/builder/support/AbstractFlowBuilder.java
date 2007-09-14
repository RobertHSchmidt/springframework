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
package org.springframework.webflow.engine.builder.support;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderException;

/**
 * Abstract base implementation of a flow builder defining common functionality needed by most concrete flow builder
 * implementations. This class implements all optional parts of the FlowBuilder process as no-op methods. Subclasses are
 * only required to implement {@link #init(String, AttributeMap)} and {@link #buildStates()}.
 * <p>
 * This class also provides a {@link FlowServiceLocator} for use by subclasses in the flow construction process.
 * 
 * @see FlowServiceLocator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowBuilder implements FlowBuilder {

	/**
	 * The <code>Flow</code> built by this builder.
	 */
	private Flow flow;

	public abstract void init(String flowId, AttributeMap attributes) throws FlowBuilderException;

	public void buildVariables() throws FlowBuilderException {
	}

	public void buildInputMapper() throws FlowBuilderException {
	}

	public void buildStartActions() throws FlowBuilderException {
	}

	public void buildInlineFlows() throws FlowBuilderException {
	}

	public abstract void buildStates() throws FlowBuilderException;

	public void buildGlobalTransitions() throws FlowBuilderException {
	}

	public void buildEndActions() throws FlowBuilderException {
	}

	public void buildOutputMapper() throws FlowBuilderException {
	}

	public void buildExceptionHandlers() throws FlowBuilderException {
	}

	public Flow getFlow() {
		return flow;
	}

	public void dispose() {
		setFlow(null);
	}

	// helpers for use in subclasses

	/**
	 * Set the flow being built by this builder. Typically called during initialization to set the initial flow
	 * reference returned by {@link #getFlow()} after building.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}
}