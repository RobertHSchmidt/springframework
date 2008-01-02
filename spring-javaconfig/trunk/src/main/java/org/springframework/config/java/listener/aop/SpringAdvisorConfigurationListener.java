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

package org.springframework.config.java.listener.aop;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.SpringAdvisor;
import org.springframework.config.java.listener.ConfigurationListenerSupport;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Configuration listener to process Spring Advisors, which must be beans. This
 * does not involve the use of the AspectJ pointcut language.
 * 
 * <p/>TODO: Review along with SpringAdvice/SpringAdvisor, which are being
 * considered for deprecation. The question here is why wouldn't we use AspectJ
 * pointcuts? Is there a compelling case for this alternative approach?
 * 
 * 
 * @author Rod Johnson
 */
public class SpringAdvisorConfigurationListener extends ConfigurationListenerSupport {

	private List<String> advisorBeanNames = new LinkedList<String>();

	@Override
	public int beanCreationMethod(BeanDefinitionRegistration beanDefinitionRegistration, ConfigurationProcessor cp,
			String configurerBeanName, Class<?> configurerClass, Method m, Bean beanAnnotation) {

		if (AnnotationUtils.findAnnotation(m, SpringAdvisor.class) != null) {
			if (!Advisor.class.isAssignableFrom(m.getReturnType())) {
				throw new IllegalArgumentException(m + " is annotated with Advisor, but does not return Advisor");
			}
			advisorBeanNames.add(m.getName());
		}
		return 0;
	}

	@Override
	public boolean processBeanMethodReturnValue(ConfigurationProcessor cp, Object originallyCreatedBean, Method method,
			ProxyFactory pf) {
		for (String advisorName : advisorBeanNames) {
			try {
				Advisor advisor = (Advisor) cp.getChildBeanFactory().getBean(advisorName);
				if (AopUtils.canApply(advisor, originallyCreatedBean.getClass())) {
					pf.addAdvisor(advisor);
				}
			}
			catch (BeanCurrentlyInCreationException ex) {
				// If the advisor could affect a bean it depends on,
				// warn and skip
				// log.warn("Skipping advisor", ex);
			}
		}
		return false;
	}

}
