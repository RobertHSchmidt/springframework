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
package org.springframework.config.java.support.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.support.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.support.EnhancerMethodInvoker;
import org.springframework.config.java.support.MethodBeanWrapper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * CGLIB MethodInterceptor that applies to methods on the configuration
 * instance.
 * 
 * Purpose: subclass configuration to ensure that singleton methods return the
 * same object on subsequent invocations, including self-invocation.
 * 
 * <p/> This implementation is thread-safe.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 */
public class BeanMethodMethodInterceptor implements MethodInterceptor {

	private static final Log log = LogFactory.getLog(BeanMethodMethodInterceptor.class);

	private final ConfigurableListableBeanFactory owningBeanFactory;

	private final BeanNameTrackingDefaultListableBeanFactory childTrackingFactory;

	private final BeanNamingStrategy namingStrategy;

	private final MethodBeanWrapper beanWrapper;

	public BeanMethodMethodInterceptor(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, MethodBeanWrapper beanWrapper,
			BeanNamingStrategy namingStrategy) {

		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(childFactory);
		Assert.notNull(beanWrapper);
		Assert.notNull(namingStrategy);

		this.owningBeanFactory = owningBeanFactory;
		this.childTrackingFactory = childFactory;
		this.beanWrapper = beanWrapper;
		this.namingStrategy = namingStrategy;
	}

	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
		Bean ann = AnnotationUtils.findAnnotation(m, Bean.class);
		if (ann == null) {
			// normally this branch will not be invoked since the callback is
			// already filtered when CGLib was set up.

			return mp.invokeSuper(o, args);
		}
		else {
			return returnWrappedResultMayBeCached(o, m, args, mp);
		}
	}

	private Object returnWrappedResultMayBeCached(final Object o, final Method m, final Object[] args,
			final MethodProxy mp) throws Throwable {

		String beanName = namingStrategy.getBeanName(m);
		boolean inCreation = false;
		Object bean = null;

		synchronized (o) {
			// check the creation on the parent
			inCreation = owningBeanFactory.isCurrentlyInCreation(beanName);
			if (inCreation) {
				bean = beanWrapper.wrapResult(beanName, new EnhancerMethodInvoker() {
					public Method getMethod() {
						return m;
					}

					public Object invokeOriginalClass() throws Throwable {
						return mp.invokeSuper(o, args);
					}
				});
			}

			else {
				// but use the tracking factory when doing the calls
				bean = childTrackingFactory.getBean(beanName);
			}
		}

		if (log.isDebugEnabled())
			if (inCreation)
				log.debug(beanName + " currenty in creation, created one");
			else
				log.debug(beanName + " not in creation, asked the BF for one");
		return bean;
	}
}
