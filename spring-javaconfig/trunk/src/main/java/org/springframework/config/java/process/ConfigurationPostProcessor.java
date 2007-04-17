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

package org.springframework.config.java.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.listener.registry.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.core.Ordered;

/**
 * Post processor for use in a bean factory that can process multiple
 * configuration beans. See the ConfigurationProcessor class's documentation for
 * the semantics of a Configuration bean.
 * <p>
 * In the BeanFactoryPostProcessor implementation, this class adds factory bean
 * definitions for every Configuration bean found in the bean factory. It also
 * creates a child factory containing pointcuts and advisors required to
 * interpret Pointcut attributes. It sets the class of the Configuration bean
 * definition to be a generated subclass that caches singletons on
 * self-invocation.
 * 
 * @see org.springframework.config.java.process.ConfigurationProcessor
 * @see org.springframework.config.java.annotation.Bean
 * @see org.springframework.config.java.annotation.Configuration
 * @author Rod Johnson
 */
public class ConfigurationPostProcessor implements BeanFactoryPostProcessor, Ordered {

	protected final Log log = LogFactory.getLog(getClass());

	private ConfigurationListenerRegistry configurationListenerRegistry;

	public ConfigurationPostProcessor() {
		this.configurationListenerRegistry = new DefaultConfigurationListenerRegistry();
	}

	// TODO property for prefix, to allow to avoid namespace conlicts?
	// Could be bean name aware, so could prefix optionally

	/**
	 * Generate BeanDefinitions and add them to factory for each Configuration
	 * bean
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanNames[i]);
			if (bd instanceof RootBeanDefinition && isEligibleForConfigurationProcessing(bd)) {
				RootBeanDefinition rbd = (RootBeanDefinition) bd;
				ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(beanFactory,
						configurationListenerRegistry);

				Class<?> clazz = null;

				if (rbd.hasBeanClass())
					clazz = rbd.getBeanClass();
				else {
					// load the class (changes in the lazy loading code part of
					// spring core)

					// TODO: add support for factory-method beans
					if (rbd.getBeanClassName() != null) {
						try {
							clazz = org.springframework.util.ClassUtils.forName(rbd.getBeanClassName());
						}
						catch (ClassNotFoundException e) {
							throw new IllegalArgumentException("invalid bean class" + rbd.getBeanClassName());
						}
					}
				}
				// TODO set resourceLoader
				if (ClassUtils.isConfigurationClass(clazz, configurationListenerRegistry)) {
					configurationProcessor.generateBeanDefinitions(beanNames[i], null, clazz);
					rbd.setBeanClass(configurationProcessor.getConfigurationEnhancer().enhanceConfiguration(null, clazz));
				}
				else {
					log.debug("Bean with name '" + beanNames[i] + "' is not a configurer");
				}
			}
		}
	}

	/**
	 * Determines if the given bean definition is eligible for configuration
	 * processsing by a ConfigurationProcessor. Abstract beans are not eligible
	 * for configuration processing; they have no class name. We might include
	 * other cases too in the future.
	 */
	private boolean isEligibleForConfigurationProcessing(BeanDefinition def) {
		if (def.isAbstract()) {
			return false;
		}

		return true;
	}

	/**
	 * Guarantee to execute before any other BeanFactoryPostProcessors
	 */
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

}
