package org.springframework.webflow.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.builder.support.FlowArtifactFactory;
import org.springframework.webflow.execution.Action;

public class FlowBuilderServices implements BeanFactoryAware, ResourceLoaderAware {

	/**
	 * The factory encapsulating the creation of central Flow artifacts such as {@link Flow flows} and
	 * {@link State states}.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * The factory encapsulating the creation of bean invoking actions, actions that adapt methods on objects to the
	 * {@link Action} interface.
	 */
	private BeanInvokingActionFactory beanInvokingActionFactory;

	/**
	 * The conversion service.
	 */
	private ConversionService conversionService;

	/**
	 * The parser for parsing expression strings into expression objects.
	 */
	private ExpressionParser expressionParser;

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader;

	/**
	 * The Spring bean factory used.
	 */
	private BeanFactory beanFactory;

	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	public void setFlowArtifactFactory(FlowArtifactFactory flowArtifactFactory) {
		this.flowArtifactFactory = flowArtifactFactory;
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return beanInvokingActionFactory;
	}

	public void setBeanInvokingActionFactory(BeanInvokingActionFactory beanInvokingActionFactory) {
		this.beanInvokingActionFactory = beanInvokingActionFactory;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}