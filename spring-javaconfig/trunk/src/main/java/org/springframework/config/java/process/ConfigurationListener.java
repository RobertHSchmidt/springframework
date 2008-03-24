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

import java.lang.reflect.Method;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.core.BeanMethodReturnValueProcessor;

/**
 * SPI interface that allows extension of a ConfigurationProcessor.
 * ConfigurationMethodListener instances are notified about the processing of
 * configuration classes and the processing of methods they contain.
 * Implementations should be thread safe.
 * 
 * @author Rod Johnson
 */
interface ConfigurationListener extends BeanMethodReturnValueProcessor {

	/**
	 * Does this configurer understand the given configuration class, which
	 * isn't a regular configuration class.
	 * @param configurerClass candidate configuration class
	 * @return whether this class is understood by this configurer
	 */
	boolean understands(Class<?> configurerClass);

	/**
	 * React to the given configuration class.
	 * 
	 * @param configurationProcessor
	 * @param configurerBeanName
	 * @param configurerClass
	 * @return number of bean definitions created
	 */
	void configurationClass(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurerClass);

	/**
	 * React to the BeanDefinition and possibly customize it or change its name
	 * @param beanDefinitionRegistration bean definition registration
	 * information
	 * @param beanFactory factory owning the configuration class. This method
	 * will be called before beans are instantiated, so other objects may not be
	 * available.
	 * @param childBeanFactory child bean factory available for internal use,
	 * such as for registering infrastructural beans
	 * @param configurerBeanName bean name of the configurer class
	 * @param configurerClass configurer class
	 * @param m configuration method
	 * @param beanAnnotation bean annotation on the configuration method, which
	 * will not be null.
	 * @return number of additional bean definitions created for the existing
	 * one. The value should be different from zero if wrapping bean definitions
	 * are created besides the normal &#64;Bean discovery process
	 */
	void beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration,
			ConfigurationProcessor configurationProcessor, String configurerBeanName, Class<?> configurerClass,
			Method m, Bean beanAnnotation);

	/**
	 * React to the encountering of a non bean definition method on the
	 * configurer class. Non bean definition methods (with Bean annotations) may
	 * be significant to some configuration classes.
	 * @param configurationProcessor
	 * @param configurerBeanName bean name of the configurer class
	 * @param configurerClass configurer class
	 * @param m method on configurer class
	 * @return number of newly bean definitions created
	 */
	void otherMethod(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurerClass, Method m);

	/**
	 * Help to process the return value of a bean definition.
	 * @param originallyCreatedBean
	 * @param method
	 * @param pf simply don't modify it if necessary
	 * @return whether or not the proxy was changed. If all listeners return
	 * false, the return value may not need to be proxied.
	 */
	boolean processBeanMethodReturnValue(BeanFactory childBeanFactory, Object originallyCreatedBean, Method method,
			ProxyFactory pf);

	/**
	 * Class to hold BeanDefinition, name and any other information, to allow
	 * configuration listeners to customize the registration, change its name,
	 * etc.
	 */
	class BeanDefinitionRegistration {
		public RootBeanDefinition rbd;

		public String name;

		/**
		 * Should the bean definition be hidden or not. When hidden, the bean
		 * definition resides only in the child context.
		 */
		public boolean hide;

		public BeanDefinitionRegistration(RootBeanDefinition rbd, String name) {
			this.rbd = rbd;
			this.name = name;
		}
	}
}
