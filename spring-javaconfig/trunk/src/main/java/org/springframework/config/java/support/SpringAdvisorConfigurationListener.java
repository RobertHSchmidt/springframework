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

package org.springframework.config.java.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.SpringAdvisor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Configuration listener to process Spring Advisors, which must be beans.
 * This does not involve the use of the AspectJ pointcut language.
 * 
 * @author Rod Johnson
 */
public class SpringAdvisorConfigurationListener extends ConfigurationListenerSupport {
	
 	private List<String> advisorBeanNames = new LinkedList<String>();
 	
 	@Override
	public void beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration, 
			ConfigurableListableBeanFactory beanFactory,
			DefaultListableBeanFactory childBeanFactory,
			String configurerBeanName, Class configurerClass, Method m,
			Bean beanAnnotation) {
		
		if (AnnotationUtils.findAnnotation(m, SpringAdvisor.class) != null) {
			if (!Advisor.class.isAssignableFrom(m.getReturnType())) {
				throw new IllegalArgumentException(m  + " is annotated with Advisor, but does not return Advisor");
			}
			advisorBeanNames.add(m.getName());
		}		
	}

 	@Override
	public boolean processBeanMethodReturnValue(
			ConfigurableListableBeanFactory beanFactory,
			DefaultListableBeanFactory childFactory,
			Object originallyCreatedBean, Method method, ProxyFactory pf) {
		for (String advisorName : advisorBeanNames) {
			try {
				Advisor advisor = (Advisor) childFactory.getBean(advisorName);
				if (AopUtils.canApply(advisor, originallyCreatedBean.getClass())) {
					pf.addAdvisor(advisor);
				}
			}
			catch (BeanCurrentlyInCreationException ex) {
				// If the advisor could affect a bean it depends on,
				// warn and skip
				//log.warn("Skipping advisor", ex);
			}
		}
		return false;
	}

}
