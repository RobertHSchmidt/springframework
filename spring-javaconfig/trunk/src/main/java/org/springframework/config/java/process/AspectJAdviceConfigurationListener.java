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

package org.springframework.config.java.process;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.NotAnAtAspectException;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.config.java.annotation.Configuration;

/**
 * Configuration listener that processes AspectJ aspects.
 * 
 * @author Rod Johnson
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
class AspectJAdviceConfigurationListener extends AbstractAopConfigurationListener {

	private AspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory();

	@Override
	public boolean understands(Class<?> configurerClass) {
		// Needs to be a configuration class to be automatically picked up.
		// Otherwise would pick up external aspects.
		return aspectJAdvisorFactory.isAspect(configurerClass)
				&& configurerClass.isAnnotationPresent(Configuration.class);
	}

	/**
	 * Check whether inheritance hierarchy is consistent
	 */
	@Override
	public int configurationClass(ConfigurationProcessor cp, String configurerBeanName, Class<?> configurerClass) {
		if (aspectJAdvisorFactory.isAspect(configurerClass)) {
			aspectJAdvisorFactory.validate(configurerClass);
		}
		return 0;
	}

	@Override
	public int otherMethod(ConfigurationProcessor cp, String configurerBeanName, final Class<?> configurerClass,
			Method aspectJAdviceMethod) {

		int count = 0;
		try {
			// If it's a valid aspect, we'll continue in this method
			// Using validate() rather than isAspect() ensures that illegal
			// cases such as an extension of a concrete aspect,
			// and unsupported instantiation models are picked up
			aspectJAdvisorFactory.validate(configurerClass);
		}
		catch (NotAnAtAspectException ex) {
			// Nothing to do
			return count;
		}

		int declarationOrderInAspect = 0;
		String aspectName = "aspectName";
		Advisor pa = aspectJAdvisorFactory.getAdvisor(/* configurerClass, */aspectJAdviceMethod,
		// new PrototypeAspectInstanceFactory(childBeanFactory,
				// getConfigurerBeanName(configurerClass)));
				new BeanFactoryAspectInstanceFactory(cp.getChildBeanFactory(), getConfigurerBeanName(configurerClass),
						configurerClass), declarationOrderInAspect, aspectName);

		// TODO should handle introductions also?
		if (pa != null && (pa instanceof PointcutAdvisor)) {
			String adviceName = aspectJAdviceMethod.getName();

			Advice advice = pa.getAdvice();

			// TODO this is required to cover named pointcuts, but seems a bit
			// hacky
			if (advice == null) {
				return count;
			}

			addAdvice(adviceName, ((PointcutAdvisor) pa).getPointcut(), advice, cp);
			// added the advice as singleton
			count++;
		}

		return count;
	}

	/**
	 * Get the bean name of this configurer class.
	 * @param configClass
	 * @return
	 */
	protected String getConfigurerBeanName(Class<?> configClass) {
		return configClass.getName();
	}

}
