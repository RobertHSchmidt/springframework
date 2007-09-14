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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.CompositeConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.convert.support.GenericConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.execution.Action;

/**
 * Default flow service locator implementation.
 * 
 * @author Keith Donald
 */
public class DefaultFlowServiceLocator implements FlowServiceLocator {

	/**
	 * The factory encapsulating the creation of central Flow artifacts such as {@link Flow flows} and
	 * {@link State states}.
	 */
	private FlowArtifactFactory flowArtifactFactory = new FlowArtifactFactory();

	/**
	 * The factory encapsulating the creation of bean invoking actions, actions that adapt methods on objects to the
	 * {@link Action} interface.
	 */
	private BeanInvokingActionFactory beanInvokingActionFactory = new BeanInvokingActionFactory();

	/**
	 * The parser for parsing expression strings into expression objects.
	 */
	private ExpressionParser expressionParser = DefaultExpressionParserFactory.getExpressionParser();

	/**
	 * The conversion service configured by the user (none by default).
	 */
	private ConversionService userConversionService = null;

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService = createConversionService(userConversionService);

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * The registry for locating subflows.
	 */
	private FlowDefinitionLocator subflowLocator;

	/**
	 * The Spring bean factory used.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow service locator that retrieves subflows from the provided registry and additional artifacts from
	 * the provided bean factory.
	 * @param subflowRegistry the registry for loading subflows
	 * @param beanFactory the spring bean factory
	 */
	public DefaultFlowServiceLocator(FlowDefinitionLocator subflowRegistry, BeanFactory beanFactory) {
		Assert.notNull(subflowRegistry, "The subflow registry is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.subflowLocator = subflowRegistry;
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the factory encapsulating the creation of central Flow artifacts such as {@link Flow flows} and
	 * {@link State states}.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		Assert.notNull(flowArtifactFactory, "The flow artifact factory is required");
		this.flowArtifactFactory = flowArtifactFactory;
	}

	/**
	 * Sets the factory for creating bean invoking actions, actions that adapt methods on objects to the {@link Action}
	 * interface.
	 */
	public void setBeanInvokingActionFactory(BeanInvokingActionFactory beanInvokingActionFactory) {
		Assert.notNull(beanInvokingActionFactory, "The bean invoking action factory is required");
		this.beanInvokingActionFactory = beanInvokingActionFactory;
	}

	/**
	 * Set the expression parser responsible for parsing expression strings into evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		Assert.notNull(expressionParser, "The expression parser is required");
		this.expressionParser = expressionParser;
		// this has impact on the TextToExpression converter in the conversion service!
		this.conversionService = createConversionService(userConversionService);
	}

	/**
	 * Set the conversion service to use to convert between types; typically from string to a rich object type.
	 */
	public void setConversionService(ConversionService userConversionService) {
		Assert.notNull(userConversionService, "The conversion service is required");
		this.userConversionService = userConversionService;
		this.conversionService = createConversionService(userConversionService);
	}

	/**
	 * Set the resource loader to load file-based resources from string-encoded paths. This is optional.
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		try {
			return (Flow) subflowLocator.getFlowDefinition(FlowId.valueOf(id));
		} catch (NoSuchFlowDefinitionException e) {
			throw new FlowArtifactLookupException(id, Flow.class, "Could not locate subflow definition with id '" + id
					+ "'", e);
		}
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		return (Action) getBean(id, Action.class);
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		return (FlowAttributeMapper) getBean(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		return (TransitionCriteria) getBean(id, TransitionCriteria.class);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException {
		return (TargetStateResolver) getBean(id, TargetStateResolver.class);
	}

	public FlowExecutionExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		return (FlowExecutionExceptionHandler) getBean(id, FlowExecutionExceptionHandler.class);
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return beanInvokingActionFactory;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	// overridable by subclasses

	protected FlowDefinitionLocator getSubflowLocator() {
		return subflowLocator;
	}

	/**
	 * Setup a conversion service used by this flow service locator.
	 * @param userConversionService a user supplied conversion service, optional
	 * @return the newly created conversion service
	 */
	protected ConversionService createConversionService(ConversionService userConversionService) {
		DefaultConversionService defaultConversionService = new DefaultConversionService();
		addWebFlowConverters(defaultConversionService);
		if (userConversionService != null) {
			return new CompositeConversionService(new ConversionService[] { userConversionService,
					defaultConversionService });
		} else {
			return defaultConversionService;
		}
	}

	// internal helpers

	/**
	 * Add all web flow specific converters to given conversion service.
	 */
	private void addWebFlowConverters(GenericConversionService conversionService) {
		conversionService.addConverter(new TextToTransitionCriteria(this));
		conversionService.addConverter(new TextToTargetStateResolver(this));
		conversionService.addConverter(new TextToExpression(getExpressionParser()));
		conversionService.addConverter(new TextToMethodSignature(conversionService));
	}

	private Object getBean(String id, Class artifactType) throws FlowArtifactLookupException {
		try {
			return getBeanFactory().getBean(id, artifactType);
		} catch (BeansException e) {
			throw new FlowArtifactLookupException(id, artifactType, e);
		}
	}

}