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

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.execution.factory.ConditionalFlowExecutionListenerLoader;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;execution-listeners&gt;</code>
 * tag.
 * 
 * @author Ben Hale
 */
class ExecutionListenersBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String LISTENERS = "listeners";
	
	private static final String LISTENER = "listener";

	private static final String CRITERIA = "criteria";

	private static final String REF = "ref";

	protected Class getBeanClass(Element element) {
		return ConditionalFlowExecutionListenerLoader.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List listenerElements = DomUtils.getChildElementsByTagName(element, LISTENER);
		definitionBuilder.addPropertyValue(LISTENERS, getListenersAndCriteria(listenerElements));
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
}