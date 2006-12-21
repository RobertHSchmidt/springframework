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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.util.Assert;
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
	 * A logger instance that can be used in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

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
	 * Returns the artifact locator.
	 */
	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	/**
	 * Returns the artifact locator
	 * @throws IllegalStateException if the artifact locator is not set
	 */
	protected FlowArtifactFactory getRequiredFlowArtifactFactory() {
		if (flowArtifactFactory == null) {
			throw new IllegalStateException("The 'flowArtifactFactory' property must be set before you can use it to "
					+ "load actions, attribute mappers, subflows, and other Flow artifacts needed by this builder");
		}
		return getFlowArtifactFactory();
	}

	/**
	 * Sets the artifact locator.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		Assert.notNull(flowArtifactFactory, "The flow artifact factory is required");
		this.flowArtifactFactory = flowArtifactFactory;
	}

	/**
	 * Returns a conversion executor capable of converting string objects to the
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable
	 * converter exists for given alias
	 */
	protected ConversionExecutor fromStringToAliased(String targetAlias) {
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

	/**
	 * Get the flow (result) built by this builder.
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow being built by this builder.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public void buildPostProcess() {
		// nothing by default
	}

	public void dispose() {
		// nothing by default
	}

	public Flow getResult() {
		return getFlow();
	}
}