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

package org.springframework.beans.factory.java.template;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.config.CompositePropertySource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Convenient base class for Configurations, allowing easy lookup of beans in
 * the owning factory
 * 
 * @author Rod Johnson
 */
@Configuration
public class ConfigurationSupport extends CompositePropertySource implements BeanFactoryAware, ResourceLoaderAware {

	private BeanFactory beanFactory;

	private ResourceLoader resourceLoader;

	// TODO inject properties in here

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Object getBean(String beanName) {
		return beanFactory.getBean(beanName);
	}

	// TODO could be BeanPostProcessor to evaluate each bean in the factory for
	// whether it's a property source,
	// calling add as necessary. Or do we want to keep them distinct?

	// TODO how to fix this for other callbacks. Can reuse any code from core?
	// Create a child context?? just with this bean?
	protected Object getObject(FactoryBean fb) {
		try {
			if (fb instanceof InitializingBean) {
				((InitializingBean) fb).afterPropertiesSet();
			}
			return fb.getObject();
		}
		catch (Exception ex) {
			throw new BeanDefinitionStoreException("Problem with FactoryBean", ex);
		}
	}

	/*
	 * protected void propertyLoad(Object getterReturn, String propertyName) {
	 * String propertyGotten = propertyName; // TODO add bean definition of this
	 * value!? Remember that it should have it? // Actually set the value!? }
	 * 
	 * protected void propertyLoad(Object getterReturn) {
	 * propertyLoad(getterReturn, null); }
	 * 
	 * private Stack<String> propertyNames = new Stack<String>();
	 * 
	 * public void __pushPropertyName(String name) {
	 * this.propertyNames.push(name); }
	 * 
	 * private String popPropertyName() { return propertyNames.pop(); }
	 */

}
