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

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * ConfigurationListener implementations that understands annotations for 
 * conversational scopes.
 * 
 * @author Rod Johnson
 */
public class ConversationScopedConfigurationListener extends ConfigurationListenerSupport {
	
	public static final String SCOPE_MAP_BEAN_NAME = "scopeMap";
	
	public static final String SCOPE_IDENTIFIER_RESOLVER_BEAN_NAME = "scopeIdentifierResolver";
	
	private static final String PROTOTYPE_PREFIX = "_prototype_";
	
	/**
	 * Change the bean definition name. 
	 * Create a new singleton bean definition of type ScopedProxyFactoryBean that "wraps"
	 * the original, prototype, bean definition.
	 * Require the presence of a scope map
	 */
	@Override
	public void beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration, 
			ConfigurableListableBeanFactory beanFactory,
			DefaultListableBeanFactory childBeanFactory,
			String configurerBeanName, Class configurerClass, Method m,
			Bean beanAnnotation) {
		if (beanAnnotation.scope() == Scope.CONVERSATIONAL) {
			
			beanDefinitionRegistration.rbd.setSingleton(false);
			// Hide the bean definition
			//beanDefinitionRegistration.hide = true;
			
			String publicName = beanDefinitionRegistration.name;
			
			beanDefinitionRegistration.name = PROTOTYPE_PREFIX + beanDefinitionRegistration.name;
			
			if (!beanFactory.containsBean(SCOPE_MAP_BEAN_NAME)) {
				throw new BeanDefinitionStoreException("Must declare bean with name '" + SCOPE_MAP_BEAN_NAME + "' to use conversational scoping");
			}

//			if (beanFactory.containsBean(SCOPE_IDENTIFIER_RESOLVER_BEAN_NAME)) {
//				sts.setScopeIdentifierResolver((ScopeIdentifierResolver) createDelegate(ScopeIdentifierResolver.class, beanFactory, SCOPE_IDENTIFIER_RESOLVER_BEAN_NAME)); 
//			}
//			

			// Register new proxy that delegates to old bean
			RootBeanDefinition tsbd = new RootBeanDefinition(ScopedProxyFactoryBean.class);
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("targetBeanName", beanDefinitionRegistration.name)).
				addPropertyValue(new PropertyValue("scopeMap", new RuntimeBeanReference(SCOPE_MAP_BEAN_NAME))).
				addPropertyValue(new PropertyValue("scopeIdentifierResolver", new RuntimeBeanReference(SCOPE_IDENTIFIER_RESOLVER_BEAN_NAME)));
			tsbd.setPropertyValues(pvs);			
			((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(publicName, tsbd);	
		}
	}
	
	

}
