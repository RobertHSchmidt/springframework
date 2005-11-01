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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.webflow.Flow;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations.
 * <p>
 * The Flow definition implementation created by this builder may be customized
 * by configuring a custom {@link FlowCreator}.
 * <p>
 * Subclasses may delegate to a configured {@link FlowArtifactLocator} to
 * resolve any externally managed flow artifacts the flow being built depends on
 * (actions, subflows, etc.)
 * 
 * @see org.springframework.webflow.config.FlowCreator
 * @see org.springframework.webflow.config.FlowArtifactLocator
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
	 * Creates the implementation of the flow built by this builder.
	 */
	private FlowCreator flowCreator = new DefaultFlowCreator();

	/**
	 * Locates actions, attribute mappers, and other artifacts usable by the
	 * flow built by this builder.
	 */
	private FlowArtifactLocator flowArtifactLocator;

	/**
	 * The conversion service to convert to flow-related artifacts, typically
	 * from string encoded representations.
	 */
	private ConversionService conversionService;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}

	/**
	 * Creates a flow builder using the locator to link in artifacts
	 * @param artifactLocator the flow artifact locator.
	 */
	protected BaseFlowBuilder(FlowArtifactLocator artifactLocator) {
		setFlowArtifactLocator(artifactLocator);
	}

	/**
	 * Creates a flow builder using the provided creation and locator
	 * strategies.
	 * @param flowCreator the flow creator
	 * @param artifactLocator the flow artifact locator.
	 */
	protected BaseFlowBuilder(FlowCreator flowCreator, FlowArtifactLocator artifactLocator) {
		setFlowCreator(flowCreator);
		setFlowArtifactLocator(artifactLocator);
	}

	/**
	 * Returns the flow creator.
	 */
	protected FlowCreator getFlowCreator() {
		return flowCreator;
	}

	/**
	 * Sets the flow creator.
	 */
	public void setFlowCreator(FlowCreator flowCreator) {
		this.flowCreator = flowCreator;
	}

	/**
	 * Returns the artifact locator.
	 */
	protected FlowArtifactLocator getFlowArtifactLocator() {
		return flowArtifactLocator;
	}

	/**
	 * Returns the artifact locator
	 * @throws an IllegalStateException if the artifact locator is not set
	 */
	protected FlowArtifactLocator getRequiredFlowArtifactLocator() {
		if (flowArtifactLocator == null) {
			throw new IllegalStateException("The flowArtifactLocator property must be set before you can use it to "
					+ "load actions, attribute mappers, subflows, and other Flow artifacts needed by this builder");
		}
		return getFlowArtifactLocator();
	}

	/**
	 * Sets the artifact locator.
	 */
	public void setFlowArtifactLocator(FlowArtifactLocator flowArtifactLocator) {
		this.flowArtifactLocator = flowArtifactLocator;
	}

	/**
	 * Returns the conversion service.
	 */
	protected ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets the conversion service.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Initialize this builder's conversion service and register default
	 * converters. Called by subclasses who wish to use the conversion
	 * infrastructure.
	 */
	protected void initConversionService() {
		if (getConversionService() == null) {
			DefaultConversionService service = new DefaultConversionService();
			service.addConverter(new TextToTransitionCriteria(getFlowArtifactLocator()));
			service.addConverter(new TextToViewSelector(getFlowArtifactLocator(), service));
			setConversionService(service);
		}
	}

	/**
	 * Returns a conversion executor capable of converting string objects to the
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable
	 * converter exists for given alias
	 */
	protected ConversionExecutor fromStringToAliased(String targetAlias) {
		return getConversionService().getConversionExecutorByTargetAlias(String.class, targetAlias);
	}

	/**
	 * Returns a converter capable of converting a string value to the given
	 * type.
	 * @param targetType the type you wish to convert to (from a string)
	 * @return the converter
	 * @throws ConversionException when the converter cannot be found
	 */
	protected ConversionExecutor fromStringTo(Class targetType) throws ConversionException {
		return getConversionService().getConversionExecutor(String.class, targetType);
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

	public Flow getResult() {
		getFlow().resolveStateTransitionsTargetStates();
		return getFlow();
	}
}