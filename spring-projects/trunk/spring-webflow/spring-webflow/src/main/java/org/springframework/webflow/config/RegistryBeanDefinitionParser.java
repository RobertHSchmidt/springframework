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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.builder.xml.XmlFlowRegistryFactoryBean;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the <code>&lt;registry&gt;</code> tag.
 * 
 * @author Ben Hale
 */
class RegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String FLOW_LOCATIONS = "flowLocations";

	private static final String LOCATION = "location";

	private static final String PATH = "path";

	protected Class getBeanClass(Element element) {
		return XmlFlowRegistryFactoryBean.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
		List locationElements = DomUtils.getChildElementsByTagName(element, LOCATION);
		List locations = getLocations(locationElements);
		definitionBuilder.addPropertyValue(FLOW_LOCATIONS, locations.toArray(new String[locations.size()]));
	}

	private List getLocations(List locationElements) {
		List locations = new ArrayList(locationElements.size());
		for (Iterator i = locationElements.iterator(); i.hasNext();) {
			Element locationElement = (Element)i.next();
			String path = locationElement.getAttribute(PATH);
			if (StringUtils.hasText(path)) {
				locations.add(path);
			}
		}
		return locations;
	}
}