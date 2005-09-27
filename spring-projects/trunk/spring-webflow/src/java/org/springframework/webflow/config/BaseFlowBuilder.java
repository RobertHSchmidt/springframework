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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.webflow.Flow;
import org.springframework.webflow.support.FlowConversionService;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations.
 * <p>
 * The builder will use a <code>FlowServiceLocator</code> to locate and create
 * any required flow related artifacts.
 * 
 * @see org.springframework.webflow.access.FlowServiceLocator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder implements FlowBuilder, BeanFactoryAware {

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
	 * Locates actions, attribute mappers, and other artifacts invokable by the
	 * flow built by this builder.
	 */
	private FlowArtifactLocator flowArtifactLocator;

	/**
	 * The conversion service to convert to flow-related artifacts, typically
	 * from string encoded representations.
	 */
	private ConversionService conversionService = new FlowConversionService();

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}

	protected BaseFlowBuilder(FlowArtifactLocator artifactLocator) {
		setFlowArtifactLocator(artifactLocator);
	}

	protected BaseFlowBuilder(FlowCreator flowCreator, FlowArtifactLocator artifactLocator) {
		setFlowCreator(flowCreator);
		setFlowArtifactLocator(artifactLocator);
	}

	protected FlowCreator getFlowCreator() {
		return flowCreator;
	}

	public void setFlowCreator(FlowCreator flowCreator) {
		this.flowCreator = flowCreator;
	}

	protected FlowArtifactLocator getFlowArtifactLocator() {
		return flowArtifactLocator;
	}

	public void setFlowArtifactLocator(FlowArtifactLocator flowArtifactLocator) {
		this.flowArtifactLocator = flowArtifactLocator;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (flowArtifactLocator == null) {
			this.flowArtifactLocator = new BeanFactoryFlowArtifactLocator(beanFactory);
		}
	}

	/**
	 * Returns the type conversion service used by this builder.
	 */
	protected ConversionService getConversionService() {
		return conversionService;
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
		return getFlow();
	}
}