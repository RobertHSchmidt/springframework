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

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.util.Assert;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.Flow;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations. All flow related
 * artifacts are expected to be defined in the bean factory defining this flow
 * builder. Subclasses can use a
 * {@link org.springframework.webflow.builder.FlowArtifactFactory} to easily
 * access that bean factory.
 * 
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.webflow.builder.FlowArtifactFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder implements FlowBuilder {

	/**
	 * The <code>Flow</code> built by this builder.
	 */
	private Flow flow;

	/**
	 * Locates actions, attribute mappers, and other artifacts usable by the
	 * flow built by this builder.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
		setFlowArtifactFactory(new DefaultFlowArtifactFactory());
	}

	/**
	 * Creates a flow builder using the locator to link in artifacts
	 * @param flowArtifactFactory the flow artifact locator.
	 */
	protected BaseFlowBuilder(FlowArtifactFactory flowArtifactFactory) {
		setFlowArtifactFactory(flowArtifactFactory);
	}

	/**
	 * Returns the flow artifact factory.
	 */
	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	/**
	 * Sets the flow artifact factory.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		Assert.notNull(flowArtifactFactory, "The flow artifact factory is required");
		this.flowArtifactFactory = flowArtifactFactory;
	}

	/**
	 * Set the flow being built by this builder. Typically called during
	 * initialization to set the initial flow reference returned by
	 * {@link #getFlow()} after building.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public abstract void init(String id, AttributeCollection attributes) throws FlowBuilderException;

	public void buildVariables() throws FlowBuilderException {

	}

	public void buildStartActions() throws FlowBuilderException {

	}

	public void buildInputMapper() throws FlowBuilderException {

	}

	public void buildInlineFlows() throws FlowBuilderException {

	}

	public abstract void buildStates() throws FlowBuilderException;

	public void buildExceptionHandlers() throws FlowBuilderException {

	}

	public void buildGlobalTransitions() throws FlowBuilderException {

	}

	public void buildEndActions() throws FlowBuilderException {

	}

	public void buildOutputMapper() throws FlowBuilderException {

	}

	public void dispose() {
	}

	/**
	 * Get the flow (result) built by this builder.
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Returns a conversion executor capable of converting string objects to the
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable
	 * converter exists for given alias
	 */
	protected ConversionExecutor fromStringTo(String targetAlias) {
		return getFlowArtifactFactory().getConversionService().getConversionExecutorByTargetAlias(String.class,
				targetAlias);
	}

	/**
	 * Returns a converter capable of converting a string value to the given
	 * type.
	 * @param targetType the type you wish to convert to (from a string)
	 * @return the converter
	 * @throws ConversionException when the converter cannot be found
	 */
	protected ConversionExecutor fromStringTo(Class targetType) throws ConversionException {
		return getFlowArtifactFactory().getConversionService().getConversionExecutor(String.class, targetType);
	}
}