/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java.context.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.context.ConfigurationScanner;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigBeanFactoryPostProcessorRegistry;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

/**
 * Fashioned after {@link JavaConfigApplicationContext}, but for use in the web
 * tier.
 * 
 * <p/>TODO: Document
 * 
 * <p/>TODO: add type-safe getBean() method (see
 * JavaConfigApplicationContext#getBean(Class<T> type)
 * 
 * @author Chris Beams
 */
public class JavaConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext {

	protected final List<Class<?>> configClasses = new ArrayList<Class<?>>();

	protected ConfigurationScanner configurationScanner;

	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
		registerDefaultPostProcessors();
		initConfigurationScanner();
		initConfigLocations();
	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
		for (Class<?> cz : configClasses)
			if (ClassUtils.isConfigurationClass(cz))
				beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
	}

	protected void registerDefaultPostProcessors() {
		new JavaConfigBeanFactoryPostProcessorRegistry().addAllPostProcessors(this);
	}

	/**
	 * TODO: Document
	 */
	protected void initConfigurationScanner() {
		configurationScanner = new ConfigurationScanner(this);
	}

	/**
	 * Processes contents of <var>configLocations</var>, setting the values of
	 * configClasses and basePackages appropriately.
	 * 
	 * @throws IllegalArgumentException if the <code>configLocations</code>
	 * array is null, contains any null elements, or contains names of any
	 * classes that cannot be found
	 * 
	 * TODO: support base packages (only classes are supported right now)
	 */
	protected void initConfigLocations() {
		Assert.notEmpty(getConfigLocations(), "configLocations property has not been set");

		for (String configLocation : getConfigLocations()) {
			try {
				configClasses.add(Class.forName(configLocation));
			}
			catch (ClassNotFoundException ex) {
				// throw new IllegalArgumentException(ex);
			}

			// if it's not a valid class, assume it's a base package
			configClasses.addAll(configurationScanner.scanPackage(configLocation));
		}
	}

}
