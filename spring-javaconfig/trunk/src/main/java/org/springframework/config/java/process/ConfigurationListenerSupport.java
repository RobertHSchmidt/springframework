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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.config.java.annotation.Bean;

/**
 * Convenient base class for implementation of the ConfigurationListener
 * interface, offer no op implementations of all methods.
 * 
 * @author Rod Johnson
 */
abstract class ConfigurationListenerSupport implements ConfigurationListener {

	protected final Log log = LogFactory.getLog(getClass());

	public boolean understands(Class<?> configurerClass) {
		return false;
	}

	public void configurationClass(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurerClass) {
	}

	public void beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration,
			ConfigurationProcessor configurationProcessor, String configurerBeanName, Class<?> configurerClass,
			Method m, Bean beanAnnotation) {
	}

	public void otherMethod(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurerClass, Method m) {
	}

	public boolean processBeanMethodReturnValue(BeanFactory childFactory, Object originallyCreatedBean, Method method,
			ProxyFactory pf) {
		return false;
	}

}
