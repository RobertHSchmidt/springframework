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

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.SpringAdvice;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

/**
 * Configuration class to create a Spring Advisor containing
 * an AspectJ pointcut and the Spring advice returned by the method.
 * Usage:
 * <code>
 * @SpringAdvice("expression")
 * protected Advice returnsSpringAdvice() { ... }
 * </code>
 * <p>
 * It is possible to specify an advice that will match everything as follows:
 * <code>
 * @SpringAdvice(matchAll=true)
 * protected Advice returnsSpringAdvice() { ... }
 * </code>
 * 
 * @author Rod Johnson
 */
public class SpringAdviceConfigurationListener extends AbstractAopConfigurationListener {
		
	@Override
	public void beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration, 
			ConfigurableListableBeanFactory beanFactory,
			DefaultListableBeanFactory childBeanFactory,
			String configurerBeanName, Class configurerClass, Method m,
			Bean beanAnnotation) {
		
		SpringAdvice springAdvice = AnnotationUtils.findAnnotation(m, SpringAdvice.class);
		if (springAdvice == null) {
			return;
		}
		
		log.debug("Found advice method " + m);

//		// Create a bean definition from the method
		// Already added, so don't need this
//		RootBeanDefinition rbd = new RootBeanDefinition(m.getReturnType());
//		rbd.setFactoryBeanName(configurerBeanName);
//		rbd.setFactoryMethodName(m.getName());
//		copyAttributes(springAdvice, rbd);
//		
//		rbd.setResourceDescription("Advice method " + m.getName() + " in class " + 
//				m.getDeclaringClass().getName());
//		
		String adviceName = m.getName();
	
		Pointcut pc;
		if (springAdvice.matchAll()) {
			pc = Pointcut.TRUE;
		}
		else {
			pc = createSpringPointcut(springAdvice, m);
		}
		addAdvice(adviceName, pc, childBeanFactory);
	}


	protected Pointcut createSpringPointcut(SpringAdvice ann, Method method) {
		AspectJExpressionPointcut ajexp;
		Order order = method.getAnnotation(Order.class);
		if (order != null) {
			ajexp = new OrderedAspectJExpressionPointcut(order.value());
		}
		else {
			ajexp = new AspectJExpressionPointcut();
		}
		ajexp.setExpression(ann.value());
		return ajexp;
	}

	/**
	 * Special pointcut used to add Ordered interface implementation to
	 * regular AspectJ expression pointcut
	 * TODO consider moving into AspectJ expression pointcut to handle precedence
	 *
	 */
	private class OrderedAspectJExpressionPointcut extends AspectJExpressionPointcut implements Ordered {
		
		private final int order;
		
		public OrderedAspectJExpressionPointcut(int order) {
			this.order = order;
		}

		public int getOrder() {
			return order;
		}
	}
}
