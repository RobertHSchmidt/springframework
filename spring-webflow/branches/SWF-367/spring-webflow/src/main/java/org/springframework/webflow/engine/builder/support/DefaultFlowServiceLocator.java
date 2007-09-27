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
import org.springframework.binding.convert.support.GenericConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;

public class DefaultFlowServiceLocator implements FlowServiceLocator {

	private FlowDefinitionLocator subflowLocator;

	private ConversionService conversionService;

	private FlowBuilderServices builderServices;

	public DefaultFlowServiceLocator(FlowDefinitionLocator subflowRegistry) {
		this(subflowRegistry, FlowBuilderSystemDefaults.get());
	}

	public DefaultFlowServiceLocator(FlowDefinitionLocator subflowRegistry, FlowBuilderServices builderServices) {
		Assert.notNull(subflowRegistry, "The subflow registry is required");
		Assert.notNull(builderServices, "The flow builder services holder is required");
		this.subflowLocator = subflowRegistry;
		this.builderServices = builderServices;
		this.conversionService = createConversionService(builderServices.getConversionService());
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		try {
			return (Flow) subflowLocator.getFlowDefinition(id);
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

	public ViewFactory getViewFactory(String id) throws FlowArtifactLookupException {
		return (ViewFactory) getBean(id, ViewFactory.class);
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		return builderServices.getFlowArtifactFactory();
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return builderServices.getBeanInvokingActionFactory();
	}

	public ExpressionParser getExpressionParser() {
		return builderServices.getExpressionParser();
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public ResourceLoader getResourceLoader() {
		return builderServices.getResourceLoader();
	}

	public BeanFactory getBeanFactory() {
		return builderServices.getBeanFactory();
	}

	public ViewFactoryCreator getViewFactoryCreator() {
		return builderServices.getViewFactoryCreator();
	}

	// usable by subclasses

	protected final FlowDefinitionLocator getSubflowLocator() {
		return subflowLocator;
	}

	// overridable by subclasses

	protected ConversionService createConversionService(ConversionService parent) {
		GenericConversionService conversionService = new GenericConversionService();
		addWebFlowConverters(conversionService);
		conversionService.setParent(parent);
		return conversionService;
	}

	// internal helpers

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