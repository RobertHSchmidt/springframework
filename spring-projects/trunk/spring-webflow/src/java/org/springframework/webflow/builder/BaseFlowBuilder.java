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
package org.springframework.webflow.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
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
	 * The conversion service to convert to flow-related artifacts, typically
	 * from string encoded representations.
	 */
	private ConversionService conversionService;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
		setFlowArtifactFactory(new FlowArtifactFactoryAdapter());
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
	 * @throws an IllegalStateException if the artifact locator is not set
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
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactLocator) {
		this.flowArtifactFactory = flowArtifactLocator;
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
			service.addConverter(new TextToTransitionCriteria(getFlowArtifactFactory()));
			service.addConverter(new TextToViewSelector(getFlowArtifactFactory(), service));
			service.addConverter(new TextToTransitionTargetStateResolver(getFlowArtifactFactory()));
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

	public void buildPostProcess() {
		
	}

	public void dispose() {
		// nothing by default
	}

	public Flow getResult() {
		return getFlow();
	}
}