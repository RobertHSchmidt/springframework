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
package org.springframework.webflow.engine.builder.xml;

import java.util.Stack;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.builder.support.FlowArtifactFactory;
import org.springframework.webflow.engine.builder.support.FlowArtifactLookupException;
import org.springframework.webflow.engine.builder.support.FlowServiceLocator;
import org.springframework.webflow.engine.builder.support.ViewFactoryCreator;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;

/**
 * Flow service locator that searches flow-local registries first before querying the global, externally managed flow
 * service locator.
 * <p>
 * Internal helper class of the {@link org.springframework.webflow.engine.builder.xml.XmlFlowBuilder}. Package private
 * to highlight it's non-public nature.
 * 
 * @see org.springframework.webflow.engine.builder.xml.XmlFlowBuilder
 * 
 * @author Keith Donald
 */
class LocalFlowServiceLocator implements FlowServiceLocator {

	/**
	 * The stack of registries.
	 */
	private Stack localRegistries = new Stack();

	/**
	 * The parent service locator.
	 */
	private FlowServiceLocator parent;

	/**
	 * Creates a new local service locator.
	 * @param parent the parent service locator
	 */
	public LocalFlowServiceLocator(FlowServiceLocator parent) {
		this.parent = parent;
	}

	/**
	 * Push a new registry onto the stack.
	 * @param registry the local registry
	 */
	public void push(LocalFlowServiceRegistry registry) {
		localRegistries.push(registry);
	}

	/**
	 * Pops all registries off the stack until the stack is empty.
	 */
	public void diposeOfAnyRegistries() {
		while (!localRegistries.isEmpty()) {
			pop();
		}
	}

	/**
	 * Pop a registry off the stack.
	 */
	public LocalFlowServiceRegistry pop() {
		return (LocalFlowServiceRegistry) localRegistries.pop();
	}

	/**
	 * Returns the top registry on the stack.
	 */
	public LocalFlowServiceRegistry top() {
		return (LocalFlowServiceRegistry) localRegistries.peek();
	}

	/**
	 * Returns true if this locator has no local registries.
	 */
	public boolean isEmpty() {
		return localRegistries.isEmpty();
	}

	// implementing FlowServiceLocator

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		Flow currentFlow = getCurrentFlow();
		// quick check for recursive subflow
		if (currentFlow.getId().equals(id)) {
			return currentFlow;
		}
		// check local inline flows
		if (currentFlow.containsInlineFlow(id)) {
			return currentFlow.getInlineFlow(id);
		}
		// check externally managed top-level flows
		return parent.getSubflow(id);
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (Action) getBean(id, Action.class);
		} else {
			return parent.getAction(id);
		}
	}

	public ViewFactory getViewFactory(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (ViewFactory) getBean(id, ViewFactory.class);
		} else {
			return parent.getViewFactory(id);
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (FlowAttributeMapper) getBean(id, FlowAttributeMapper.class);
		} else {
			return parent.getAttributeMapper(id);
		}
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (TransitionCriteria) getBean(id, TransitionCriteria.class);
		} else {
			return parent.getTransitionCriteria(id);
		}
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (TargetStateResolver) getBean(id, TargetStateResolver.class);
		} else {
			return parent.getTargetStateResolver(id);
		}
	}

	public FlowExecutionExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		if (containsBean(id)) {
			return (FlowExecutionExceptionHandler) getBean(id, FlowExecutionExceptionHandler.class);
		} else {
			return parent.getExceptionHandler(id);
		}
	}

	// TODO - use flow builder services here?
	public FlowArtifactFactory getFlowArtifactFactory() {
		if (containsBean("flowArtifactFactory")) {
			return (FlowArtifactFactory) getBean("flowArtifactFactory", FlowArtifactFactory.class);
		} else {
			return parent.getFlowArtifactFactory();
		}
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		if (containsBean("beanInvokingActionFactory")) {
			return (BeanInvokingActionFactory) getBean("beanInvokingActionFactory", BeanInvokingActionFactory.class);
		} else {
			return parent.getBeanInvokingActionFactory();
		}
	}

	public ViewFactoryCreator getViewFactoryCreator() {
		if (containsBean("viewFactoryCreator")) {
			return (ViewFactoryCreator) getBean("viewFactoryCreator", ViewFactoryCreator.class);
		} else {
			return parent.getViewFactoryCreator();
		}
	}

	public ExpressionParser getExpressionParser() {
		if (containsBean("expressionParser")) {
			return (ExpressionParser) getBean("expressionParser", ExpressionParser.class);
		} else {
			return parent.getExpressionParser();
		}
	}

	public ConversionService getConversionService() {
		if (containsBean("conversionService")) {
			return (ConversionService) getBean("conversionService", ConversionService.class);
		} else {
			return parent.getConversionService();
		}
	}

	public ResourceLoader getResourceLoader() {
		if (top().getBeanFactory() instanceof ResourceLoader) {
			return (ResourceLoader) top().getBeanFactory();
		} else {
			return parent.getResourceLoader();
		}
	}

	public BeanFactory getBeanFactory() {
		return top().getBeanFactory();
	}

	// internal helpers

	/**
	 * Returns the flow for the registry at the top of the stack.
	 */
	private Flow getCurrentFlow() {
		return top().getFlow();
	}

	/**
	 * Does this flow local service locator contain a bean defintion for the given id?
	 */
	private boolean containsBean(String id) {
		if (localRegistries.isEmpty()) {
			return false;
		} else {
			return getBeanFactory().containsBean(id);
		}
	}

	/**
	 * Get the identified bean and make sure it is of the required type.
	 */
	private Object getBean(String id, Class artifactType) throws FlowArtifactLookupException {
		try {
			return getBeanFactory().getBean(id, artifactType);
		} catch (BeansException e) {
			throw new FlowArtifactLookupException(id, artifactType, e);
		}
	}
}