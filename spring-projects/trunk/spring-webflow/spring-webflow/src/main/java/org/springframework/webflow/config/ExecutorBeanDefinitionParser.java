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
package org.springframework.webflow.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;swf:executor&gt;</code> tag.
 * 
 * @author Ben Hale
 */
public class ExecutorBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String EXECUTION_LISTENER_LOADER = "executionListenerLoader";

	private static final String LISTENER = "listener";

	private static final String CRITERIA = "criteria";

	private static final String REF = "ref";

	private static final String REGISTRY_REF = "registry-ref";

	private static final String REPOSITORY_TYPE = "repositoryType";

	protected BeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(FlowExecutorFactoryBean.class);
		definitionBuilder.addConstructorArgReference(getRegistryRef(element));
		definitionBuilder.addPropertyReference(EXECUTION_LISTENER_LOADER, buildFlowExecutionListenerLoader(element,
				parserContext));
		definitionBuilder.addPropertyValue(REPOSITORY_TYPE, getRepositoryType(element));
		return definitionBuilder.getBeanDefinition();
	}

	/**
	 * Creates either a
	 * {@link ConditionalFlowExecutionListenerLoaderFactoryBean} bean definition
	 * and registers it in the application context.
	 * @param element The element to extract the listeners from
	 * @param parserContext The parser context containing the registry to place
	 * the bean definition in
	 * @return The anonymous name of the bean after it was registered
	 */
	private String buildFlowExecutionListenerLoader(Element element, ParserContext parserContext) {
		List listeners = DomUtils.getChildElementsByTagName(element, LISTENER);
		BeanDefinitionBuilder definitionBuilder;
		if (!listeners.isEmpty()) {
			definitionBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(ConditionalFlowExecutionListenerLoaderFactoryBean.class);
			definitionBuilder.addConstructorArg(getListenersAndCriteria(listeners));
		}
		else {
			definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(StaticFlowExecutionListenerLoader.class);
		}
		return registerAndReturnBeanName(definitionBuilder, parserContext);
	}

	/**
	 * Returns the name of the registry detailed in the bean definition
	 * @param element The element to extract the registry name from
	 * @return The name of the registry
	 */
	private String getRegistryRef(Element element) {
		String registryRef = element.getAttribute(REGISTRY_REF);
		if (!StringUtils.hasText(registryRef)) {
			throw new IllegalArgumentException("The registry-ref of the <swf:executor> element must have a value");
		}
		return registryRef;
	}

	/**
	 * Returns the name of the registry detailed in the bean definition
	 * @param element The element to extract the registry name from
	 * @return The name of the registry
	 */
	private String getRepositoryType(Element element) {
		return StringUtils.capitalize(element.getAttribute(REPOSITORY_TYPE));
	}

	/**
	 * Creates a map of listeners and their criteria.
	 * @param listeners The list of listener elements from the bean definition
	 * @return A map containing keys that are references to a given listeners
	 * and values of string that represent the criteria
	 */
	private Map getListenersAndCriteria(List listeners) {
		Map listenersAndCriteria = new ManagedMap(listeners.size());
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Element listenerElement = (Element)i.next();
			RuntimeBeanReference ref = new RuntimeBeanReference(listenerElement.getAttribute(REF));
			String criteria = listenerElement.getAttribute(CRITERIA);
			listenersAndCriteria.put(ref, criteria);
		}
		return listenersAndCriteria;
	}

	/**
	 * Generates a unique anonymous name for the bean and registers it in the
	 * application context.
	 * @param definitionBuilder The builder for the bean definition
	 * @param parserContext The parser context containing the registry to place
	 * the bean definition in
	 * @return The anonymous name of the bean after it was registered
	 */
	private String registerAndReturnBeanName(BeanDefinitionBuilder definitionBuilder, ParserContext parserContext) {
		AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
		String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, parserContext.getRegistry(), true);
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
		parserContext.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(beanDefinition, beanName));
		return beanName;
	}
}
