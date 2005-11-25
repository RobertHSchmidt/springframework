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
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.webflow.Flow;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations. All flow related artifacts
 * are expected to be defined in the bean factory defining this flow builder.
 * Subclasses can use a {@link org.springframework.webflow.config.FlowArtifactFactory}
 * to easily access that bean factory.
 * 
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.webflow.config.FlowArtifactFactory
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
	 * The conversion service to convert to flow-related artifacts, typically
	 * from string encoded representations.
	 */
	private ConversionService conversionService;

	/**
	 * The bean factory defining this flow builder.
	 */
	private BeanFactory beanFactory;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}
	
	/**
	 * Create a new flow builder looking up required flow artifacts
	 * in given bean factory.
	 * @param beanFactory the bean factory to be used, typically the bean
	 * factory defining this flow builder
	 */
	protected BaseFlowBuilder(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * Returns the bean factory defining this flow builder.
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
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
			service.addConverter(new TextToTransitionCriteria());
			service.addConverter(new TextToViewSelector(service));
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