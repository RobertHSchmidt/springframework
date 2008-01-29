/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.config.java.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * Convenient base class for Configurations, allowing easy lookup of beans in
 * the owning factory
 * 
 * @author Rod Johnson
 */
@Configuration
public class ConfigurationSupport implements BeanFactoryAware, ApplicationContextAware {

	private BeanFactory beanFactory;

	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	private ApplicationContext applicationContext;

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof AutowireCapableBeanFactory) {
			autowireCapableBeanFactory = (AutowireCapableBeanFactory) beanFactory;
		}
	}

	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	public void setApplicationContext(ApplicationContext ac) {
		this.applicationContext = ac;
	}

	@Deprecated
	// TODO: this isn't used anywhere. Can we get rid of it?
	protected ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	public Object getBean(String beanName) {
		return beanFactory.getBean(beanName);
	}

	/*
	 * TODO: consider adding this back in once it's clear what to do with
	 * TypeSafeBeanFactory (that interface is currently package-private in the
	 * context package pending further review)
	 * 
	 * public <T> T getBean(Class<T> type) { return ((TypeSafeBeanFactory)
	 * beanFactory).getBean(type); }
	 */

	/**
	 * Return the object created by this FactoryBean instance, first invoking
	 * any container callbacks on the instance
	 * @param fb FactoryBean instance
	 * @return the object created by the configured FactoryBean instance
	 */
	protected Object getObject(FactoryBean fb) {
		try {
			return ((FactoryBean) getConfigured(fb)).getObject();
		}
		catch (Exception ex) {
			// TODO clean up
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Invoke callbacks on the object, as though it was configured in the
	 * factory
	 * @param o object to configure
	 * @return object after callbacks have been called on it
	 */
	protected Object getConfigured(Object o) {
		if (this.autowireCapableBeanFactory == null) {
			throw new UnsupportedOperationException(
					"Cannot configure object - not running in an AutowireCapableBeanFactory");
		}

		autowireCapableBeanFactory.initializeBean(o, null);

		// TODO could replace with ApplicationContextAwareProcessor call if that
		// class were public
		if (this.applicationContext != null) {
			if (o instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) o).setResourceLoader(this.applicationContext);
			}
			if (o instanceof ApplicationEventPublisherAware) {
				((ApplicationEventPublisherAware) o).setApplicationEventPublisher(this.applicationContext);
			}
			if (o instanceof MessageSourceAware) {
				((MessageSourceAware) o).setMessageSource(this.applicationContext);
			}
			if (o instanceof ApplicationContextAware) {
				((ApplicationContextAware) o).setApplicationContext(this.applicationContext);
			}
		}

		return o;
	}

	// TODO could be BeanPostProcessor to evaluate each bean in the factory for
	// whether it's a property source,
	// calling add as necessary. Or do we want to keep them distinct?
	// TODO how to fix this for other callbacks. Can reuse any code from core?
	// Create a child context?? just with this bean?
}
