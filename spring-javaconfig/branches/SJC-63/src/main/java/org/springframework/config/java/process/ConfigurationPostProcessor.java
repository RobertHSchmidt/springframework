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
package org.springframework.config.java.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ResourceLoader;

/**
 * Post processor for use in a bean factory that can process multiple
 * configuration beans. See the ConfigurationProcessor class's documentation for
 * the semantics of a Configuration bean.
 * <p>
 * In the BeanFactoryPostProcessor implementation, this class adds factory bean
 * definitions for every Configuration bean found in the bean factory. It also
 * creates a child factory containing pointcuts and advisors required to
 * interpret Pointcut attributes.
 * 
 * @see org.springframework.config.java.process.ConfigurationProcessor
 * @see org.springframework.config.java.annotation.Bean
 * @see org.springframework.config.java.annotation.Configuration
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 */
public class ConfigurationPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered, ResourceLoaderAware,
		ApplicationContextAware {

	protected final Log log = LogFactory.getLog(getClass());

	private ProcessingContext pc = new ProcessingContext();

	private ConfigurationListenerRegistry configurationListenerRegistry;

	private ConfigurableApplicationContext owningApplicationContext;

	private boolean hasRun;

	/**
	 * Guarantee execution occurs before any other BeanFactoryPostProcessors
	 */
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	/**
	 * The listener registry used by this factory bean. Initially,
	 * {@link DefaultConfigurationListenerRegistry} is used.
	 * 
	 * @param configurationListenerRegistry The configurationListenerRegistry to
	 * set.
	 */
	public void setConfigurationListenerRegistry(ConfigurationListenerRegistry configurationListenerRegistry) {
		this.configurationListenerRegistry = configurationListenerRegistry;
	}

	/**
	 * BeanNamingStrategy used for generating the bean definitions.
	 * 
	 * @param namingStrategy The namingStrategy to set.
	 */
	public void setNamingStrategy(BeanNamingStrategy namingStrategy) {
		pc.beanNamingStrategy = namingStrategy;
	}

	/**
	 * Optional implementation of ResourceLoaderAware
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		pc.resourceLoader = resourceLoader;
	}

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		if (ctx instanceof ConfigurableApplicationContext) {
			this.owningApplicationContext = (ConfigurableApplicationContext) ctx;
		}
	}

	/**
	 * Generate BeanDefinitions and add them to factory for each Configuration
	 * bean.
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (hasRun)
			throw new IllegalStateException("ConfigurationPostProcessor cannot run on two BeanFactories");

		hasRun = true;

		String[] beanNames = beanFactory.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			ConfigurationProcessor processor = new ConfigurationProcessor();
			processor.ac = this.owningApplicationContext;
			processor.owningBeanFactory = beanFactory;
			processor.pc = pc;
			if (configurationListenerRegistry != null)
				processor.setConfigurationListenerRegistry(configurationListenerRegistry);
			processor.afterPropertiesSet();

			Class<?> clazz = ProcessUtils.getBeanClass(beanName, beanFactory);

			if (processor.isConfigClass(clazz))
				processor.processConfigurationBean(beanName, clazz);
		}

	}

}
