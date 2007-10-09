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

import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.builder.FlowBuilderException;

/**
 * Abstract base implementation of a flow builder defining common functionality needed by most concrete flow builder
 * implementations. This class implements all optional parts of the FlowBuilder process as no-op methods. Subclasses are
 * only required to implement {@link #init(FlowBuilderContext)} and {@link #buildStates()}.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowBuilder implements FlowBuilder {

	/**
	 * The <code>Flow</code> built by this builder.
	 */
	private Flow flow;

	private FlowBuilderContext context;

	protected FlowBuilderContext getContext() {
		return context;
	}

	public void init(FlowBuilderContext context) throws FlowBuilderException {
		this.context = context;
		doInit();
		this.flow = createFlow();
	}

	protected void doInit() {

	}

	protected abstract Flow createFlow();

	public void buildVariables() throws FlowBuilderException {
	}

	public void buildInputMapper() throws FlowBuilderException {
	}

	public void buildStartActions() throws FlowBuilderException {
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
		flow = null;
		doDispose();
	}

	protected void doDispose() {

	}

}