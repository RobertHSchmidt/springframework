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

import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the
 * <code>&lt;execution-attributes&gt;</code> tag.
 * 
 * @author Ben Hale
 */
class ExecutionAttributesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String SOURCE_MAP_PROPERTY = "sourceMap";

	private static final String ATTRIBUTE = "attribute";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String VALUE = "value";

	protected Class getBeanClass(Element element) {
		return MapFactoryBean.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List attributeElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE);
		Map attributeMap = new ManagedMap(attributeElements.size());
		putAttributes(attributeMap, attributeElements);
		putSpecialAttributes(attributeMap, element);
		definitionBuilder.addPropertyValue(SOURCE_MAP_PROPERTY, attributeMap);
	}

	private void putAttributes(Map attributeMap, List attributeElements) {
		for (Iterator i = attributeElements.iterator(); i.hasNext();) {
			Element attributeElement = (Element)i.next();
			String type = attributeElement.getAttribute(TYPE);
			Object value;
			if (StringUtils.hasText(type)) {
				value = new TypedStringValue(attributeElement.getAttribute(VALUE), type);
			} else {
				value = attributeElement.getAttribute(VALUE);
			}
			attributeMap.put(attributeElement.getAttribute(NAME), value);
		}
	}

	private void putSpecialAttributes(Map attributeMap, Element element) {
		putAlwaysRedirectOnPauseAttribute(attributeMap, DomUtils.getChildElementByTagName(element, ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE));
	}

	private void putAlwaysRedirectOnPauseAttribute(Map attributeMap, Element element) {
		if (element != null) {
			Boolean value = Boolean.valueOf(element.getAttribute(VALUE));
			attributeMap.put(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE, value);
		}
	}
}