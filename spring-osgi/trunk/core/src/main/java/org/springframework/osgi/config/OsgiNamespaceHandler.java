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

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.osgi.service.importer.OsgiMultiServiceProxyFactoryBean;

/**
 * Namespace handler for Osgi definitions.
 * 
 * @author Hal Hildebrand
 * @author Andy Piper
 * @author Costin Leau
 */
public class OsgiNamespaceHandler extends NamespaceHandlerSupport {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	public void init() {
		registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());

		registerBeanDefinitionParser("collection", new CollectionBeanDefinitionParser());
		registerBeanDefinitionParser("list", new CollectionBeanDefinitionParser() {
			protected int getCollectionType() {
				return OsgiMultiServiceProxyFactoryBean.CollectionOptions.LIST;
			}
		});
		
		registerBeanDefinitionParser("list", new CollectionBeanDefinitionParser() {
			protected int getCollectionType() {
				return OsgiMultiServiceProxyFactoryBean.CollectionOptions.LIST;
			}
		});

		registerBeanDefinitionParser("set", new CollectionBeanDefinitionParser() {
			protected int getCollectionType() {
				return OsgiMultiServiceProxyFactoryBean.CollectionOptions.SET;
			}
		});

		registerBeanDefinitionParser("sorted-list", new CollectionBeanDefinitionParser() {
			protected int getCollectionType() {
				return OsgiMultiServiceProxyFactoryBean.CollectionOptions.SORTED_LIST;
			}
		});

		registerBeanDefinitionParser("sorted-set", new CollectionBeanDefinitionParser() {
			protected int getCollectionType() {
				return OsgiMultiServiceProxyFactoryBean.CollectionOptions.SORTED_SET;
			}
		});

		registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());

		registerBeanDefinitionParser("property-placeholder", new OsgiPropertyPlaceholderDefinitionParser());

		registerBeanDefinitionParser("config", new OsgiConfigDefinitionParser());

		registerBeanDefinitionParser("bundle", new BundleBeanDefinitionParser());

	}
}
