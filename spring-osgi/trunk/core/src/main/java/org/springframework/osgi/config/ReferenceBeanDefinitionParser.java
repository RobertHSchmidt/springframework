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
 *
 */
package org.springframework.osgi.config;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.osgi.config.ParserUtils.AttributeCallback;
import org.springframework.osgi.service.importer.OsgiSingleServiceProxyFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * &lt;osgi:reference&gt; tag parser.
 * 
 * @author Andy Piper
 * @author Costin Leau
 * @since 2.1
 */
class ReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	public static final String PROPERTIES = "properties";

	public static final String LISTENER = "listener";

	public static final String LISTENERS_PROPERTY = "listeners";

	public static final String BIND_METHOD = "bind-method";

	public static final String UNBIND_METHOD = "unbind-method";

	public static final String REF = "ref";

	public static final String INTERFACE = "interface";

	public static final String INTERFACE_NAME = "interfaceName";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	protected Class getBeanClass(Element element) {
		return OsgiSingleServiceProxyFactoryBean.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element,
	 * org.springframework.beans.factory.xml.ParserContext,
	 * org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
	protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		ParserUtils.parseCustomAttributes(element, builder, new AttributeCallback() {
			public void process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
				String name = attribute.getLocalName();
				// ref attribute will be handled separately
				builder.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
			}
		});

		parseNestedElements(element, context, builder);
	}

	protected void parseNestedElements(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		// parse subelements
		// context.getDelegate().parsePropertyElements(element,
		// builder.getBeanDefinition());
		List listeners = DomUtils.getChildElementsByTagName(element, LISTENER);

		ManagedList listenersRef = new ManagedList();
		// loop on listeners
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			Element listnr = (Element) iter.next();

			// wrapper target object
			Object target = null;

			// filter elements
			NodeList nl = listnr.getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element beanDef = (Element) node;

					// check inline ref
					if (listnr.hasAttribute(REF))
						context.getReaderContext().error(
							"nested bean declaration is not allowed if 'ref' attribute has been specified", beanDef);

					target = context.getDelegate().parsePropertySubElement(beanDef, builder.getBeanDefinition());
				}
			}

			// extract bind/unbind attributes from <osgi:listener>
			// Element

			MutablePropertyValues vals = new MutablePropertyValues();

			NamedNodeMap attrs = listnr.getAttributes();
			for (int x = 0; x < attrs.getLength(); x++) {
				Attr attribute = (Attr) attrs.item(x);
				String name = attribute.getLocalName();

				if (REF.equals(name))
					target = new RuntimeBeanReference(StringUtils.trimWhitespace(attribute.getValue()));
				else
					vals.addPropertyValue(Conventions.attributeNameToPropertyName(name), attribute.getValue());
			}

			// create serviceListener wrapper
			RootBeanDefinition wrapperDef = new RootBeanDefinition(TargetSourceLifecycleListenerWrapper.class);

			ConstructorArgumentValues cav = new ConstructorArgumentValues();
			cav.addIndexedArgumentValue(0, target);

			wrapperDef.setConstructorArgumentValues(cav);
			wrapperDef.setPropertyValues(vals);
			listenersRef.add(wrapperDef);

		}

		builder.addPropertyValue(LISTENERS_PROPERTY, listenersRef);

	}
}
