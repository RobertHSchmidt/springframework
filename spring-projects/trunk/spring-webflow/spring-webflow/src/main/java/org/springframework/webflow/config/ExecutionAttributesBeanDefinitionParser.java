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

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.core.collection.AttributeMap;
import org.w3c.dom.Element;

public class ExecutionAttributesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String ATTRIBUTE = "attribute";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String VALUE = "value";

	protected Class getBeanClass(Element element) {
		return AttributeMap.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List attributeElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE);
		definitionBuilder.addConstructorArg(buildAttributeMap(attributeElements));
	}

	private Map buildAttributeMap(List attributeElements) {
		Map attributeMap = new ManagedMap(attributeElements.size());
		for (Iterator i = attributeElements.iterator(); i.hasNext();) {
			Element attributeElement = (Element)i.next();
			attributeMap.put(attributeElement.getAttribute(NAME), new TypedStringValue(attributeElement
					.getAttribute(VALUE), attributeElement.getAttribute(TYPE)));
		}
		return attributeMap;
	}
}