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

import org.springframework.webflow.Flow;

/**
 * Builder interface used to build a flow definition. The process of building a
 * flow consists of the following steps:
 * <ol>
 * <li> Initialize this builder, creating the initial flow definition, by
 * calling {@link #init(FlowArtifactParameters)}.
 * <li> Call {@link #buildStates} to create the states of the flow and add them
 * to the flow definition.
 * <li> Call {@link #buildExceptionHandlers} to create the state exception
 * handlers of the flow and add them to the flow definition.
 * <li> Call {@link #buildPostProcess} to do any build post processing, for
 * example, making a second pass through the fully configured Flow to resolve
 * any artifacts.
 * <li> Call {@link #getResult} to return the fully-built {@link Flow}
 * definition.
 * <li> Dispose this builder, releasing any resources allocated during the
 * building process by calling {@link #dispose()}.
 * </ol>
 * <p>
 * Implementations should encapsulate flow construction logic, either for a
 * specific kind of flow, for example, an <code>OrderFlowBuilder</code> built
 * in Java code, or a generic flow builder strategy, like the
 * <code>XmlFlowBuilder</code>, for building flows from an XML-definition.
 * <p>
 * Flow builders are used by the
 * {@link org.springframework.webflow.builder.FlowAssembler}, which acts as an
 * assembler (director). Flow Builders may be reused, however, exercise caution
 * when doing this as these objects are not thread safe. Also, for each use be
 * sure to call init, buildStates, buildExceptionHandlers, buildPostProcess,
 * getResult, and dispose completely in that order.
 * <p>
 * This is an example of the classic GoF Builder pattern.
 * 
 * @see org.springframework.webflow.builder.AbstractFlowBuilder
 * @see org.springframework.webflow.builder.XmlFlowBuilder
 * @see org.springframework.webflow.builder.FlowAssembler
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowBuilder {

	/**
	 * Initialize this builder and return a handle to the flow under
	 * construction.
	 * @param flowParameters flow parameters to be assigned to the flow being
	 * built
	 * @throws FlowBuilderException an exception occured building the flow
	 */
	public void init(FlowArtifactParameters flowParameters) throws FlowBuilderException;

	/**
	 * Creates and adds all states to the flow built by this builder.
	 * @throws FlowBuilderException an exception occured building the flow
	 */
	public void buildStates() throws FlowBuilderException;

	/**
	 * Creates and adds all state exception handlers to the flow built by this
	 * builder.
	 * @throws FlowBuilderException an exception occured building this flow
	 */
	public void buildExceptionHandlers() throws FlowBuilderException;

	/**
	 * Do any post processing necessary by this builder.
	 * @throws FlowBuilderException an exception occured during post processing
	 */
	public void buildPostProcess() throws FlowBuilderException;

	/**
	 * Get the fully constructed and configured Flow object - called by the
	 * builder's assembler (director) after assembly. Note that this method will
	 * return the same Flow object as that returned from the <code>init()</code>
	 * method. However, when this method is called by the assembler, flow
	 * construction will have completed and the returned flow is ready for use.
	 */
	public Flow getResult();

	/**
	 * Shutdown the builder, releasing any resources it holds. A new flow
	 * construction process should start with another call to the
	 * <code>init()</code> method.
	 */
	public void dispose();
}