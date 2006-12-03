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
package org.springframework.beans.factory.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AutoBean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Configuration listener that registers autowired bean definitions
 * in the factory for @AutoBean methods.
 * 
 * @author Rod Johnson
 */
public class AutoBeanConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class configurerClass) {
		return configurerClass.isAnnotationPresent(Configuration.class);
	}
	
	@Override
	public void otherMethod(ConfigurableListableBeanFactory beanFactory, 
				DefaultListableBeanFactory childBeanFactory, 
				String configurerBeanName, 
				Class configurerClass, 
				Method m) {
		AutoBean autoBean = AnnotationUtils.findAnnotation(m, AutoBean.class);
		if (autoBean != null) {
			// Create a bean definition for this class
			if (m.getReturnType().isInterface()) {
				throw new BeanDefinitionStoreException("Cannot use AutoBean of interface type " + m.getReturnType() +
						": don't know what class to instantiate; processing @AutoBean method " + m);
			}
			
			// TODO unsafe assumption: how do we get to be 
			// able to add bean definitions
			BeanDefinitionRegistry bdr = (BeanDefinitionRegistry) beanFactory;
			
			RootBeanDefinition bd = new RootBeanDefinition(m.getReturnType());
			bd.setAutowireMode(autoBean.autowire().value());
			if (Modifier.isPublic(m.getModifiers())) {
				bdr.registerBeanDefinition(m.getName(), bd);
			}
			else {
				// Hide the bean so that it's not publically visible
				childBeanFactory.registerBeanDefinition(m.getName(), bd);
			}
		}
	}
}
