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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;executor&gt;</code> tag.
 * 
 * @author Ben Hale
 */
class ExecutorBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String EXECUTION_ATTRIBUTES_ELEMENT = "execution-attributes";

	private static final String EXECUTION_ATTRIBUTES_PROPERTY = "executionAttributes";

	private static final String EXECUTION_LISTENER_LOADER = "executionListenerLoader";

	private static final String EXECUTION_LISTENERS = "execution-listeners";

	private static final String REGISTRY_REF = "registry-ref";

	private static final String REPOSITORY_TYPE_ATTRIBUTE = "repository-type";

	private static final String REPOSITORY_TYPE_PROPERTY = "repositoryType";

	protected BeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(FlowExecutorFactoryBean.class);
		definitionBuilder.addConstructorArgReference(getRegistryRef(element));
		addExecutionAttributes(element, parserContext, definitionBuilder);
		addExecutionListenerLoader(element, parserContext, definitionBuilder);
		definitionBuilder.addPropertyValue(REPOSITORY_TYPE_PROPERTY, getRepositoryType(element));
		return definitionBuilder.getBeanDefinition();
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
	 * Returns the name of the repository type enum field detailed in the bean
	 * definition
	 * @param element The element to extract the registry name from
	 * @return The name of the registry
	 */
	private String getRepositoryType(Element element) {
		return element.getAttribute(REPOSITORY_TYPE_ATTRIBUTE).toUpperCase();
	}

	private void addExecutionAttributes(Element element, ParserContext parserContext,
			BeanDefinitionBuilder definitionBuilder) {
		Element attributesElement = DomUtils.getChildElementByTagName(element, EXECUTION_ATTRIBUTES_ELEMENT);
		if (attributesElement != null) {
			definitionBuilder.addPropertyValue(EXECUTION_ATTRIBUTES_PROPERTY, parserContext.getDelegate()
					.parseCustomElement(attributesElement, true));
		}
	}

	private void addExecutionListenerLoader(Element element, ParserContext parserContext,
			BeanDefinitionBuilder definitionBuilder) {
		Element listenersElement = DomUtils.getChildElementByTagName(element, EXECUTION_LISTENERS);
		if (listenersElement != null) {
			definitionBuilder.addPropertyValue(EXECUTION_LISTENER_LOADER, parserContext.getDelegate()
					.parseCustomElement(listenersElement, true));
		}
	}
}