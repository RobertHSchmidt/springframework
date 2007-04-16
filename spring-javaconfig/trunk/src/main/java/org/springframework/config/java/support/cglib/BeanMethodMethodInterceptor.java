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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Scope;
import org.springframework.config.java.listener.ConfigurationListener;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.support.factory.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * CGLIB MethodInterceptor that applies to methods on the configuration
 * instance. Purpose: subclass configuration to ensure that singleton methods
 * return the same object on subsequent invocations, including self-invocation.
 * Do need one of these per intercepted class.
 * 
 * @author Rod Johnson
 */
public class BeanMethodMethodInterceptor implements MethodInterceptor {

	private ConfigurableListableBeanFactory owningBeanFactory;

	private BeanNameTrackingDefaultListableBeanFactory childTrackingFactory;

	private ConfigurationListenerRegistry configurationListenerRegistry;

	private Map<Method, Object> singletons = new HashMap<Method, Object>();

	public BeanMethodMethodInterceptor(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory,
			ConfigurationListenerRegistry configurationListenerRegistry) {

		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(configurationListenerRegistry, "configurationListenerRegistry is required");

		this.owningBeanFactory = owningBeanFactory;
		this.childTrackingFactory = childFactory;
		this.configurationListenerRegistry = configurationListenerRegistry;
	}

	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
		Bean ann = AnnotationUtils.findAnnotation(m, Bean.class);
		if (ann == null) {
			// Not a bean, don't change the method implementation
			return mp.invokeSuper(o, args);
		}
		else {
			return returnWrappedResultMayBeCached(o, m, args, mp, ann.scope() == Scope.SINGLETON);
		}
	}

	private Object returnWrappedResultMayBeCached(Object o, Method m, Object[] args, MethodProxy mp, boolean useCache)
			throws Throwable {
		if (!useCache) {
			return wrapResult(o, args, m, mp);
		}

		// Cache result, for singleton style behaviour,
		// where the bean creation method always returns the same value
		synchronized (o) {
			Object cached = singletons.get(m);
			if (cached == null) {
				cached = wrapResult(o, args, m, mp);
				singletons.put(m, cached);
			}
			return cached;
		}
	}

	/**
	 * Wrap the result of a bean definition method in a Spring AOP proxy if
	 * there are advisors in the current factory that would apply to it. Note
	 * that the advisors may have been added explicitly by the user or may have
	 * resulted from Advisor generation on this class processing a Pointcut
	 * annotation
	 * 
	 * @param o
	 * @param args
	 * @param mp
	 * @return
	 * @throws Throwable
	 */
	private Object wrapResult(Object o, Object[] args, Method m, MethodProxy mp) throws Throwable {

		// If we are in our first call to getBean() with this name and were
		// not
		// called by the factory (which would have tracked the call), call
		// the factory
		// to get the bean. We need to do this to ensure that lifecycle
		// callbacks are invoked,
		// so that calls made within a factory method in otherBean() style
		// still
		// get fully configured objects.
		String lastRequestedBeanName = childTrackingFactory.lastRequestedBeanName();
		if (lastRequestedBeanName != null && !m.getName().equals(lastRequestedBeanName)) {
			return childTrackingFactory.getBean(m.getName());
		}

		try {
			if (lastRequestedBeanName == null) {
				// Remember the getBean() method we're now executing,
				// if we were invoked from within the factory method in the
				// configuration class
				// rather than through the BeanFactory
				childTrackingFactory.recordRequestForBeanName(m.getName());
			}

			// Get raw result of @Bean method or get bean from factory if it
			// is overriden
			Object originallyCreatedBean = null;
			BeanDefinition beanDef = owningBeanFactory.getBeanDefinition(m.getName());
			if (beanDef instanceof RootBeanDefinition) {
				RootBeanDefinition rbdef = (RootBeanDefinition) beanDef;
				if (rbdef.getFactoryBeanName() == null) {
					// We have a regular bean definition already in the
					// factory:
					// use that instead of the @Bean method
					originallyCreatedBean = owningBeanFactory.getBean(m.getName());
				}
			}
			if (originallyCreatedBean == null) {
				originallyCreatedBean = mp.invokeSuper(o, args);
			}

			if (!configurationListenerRegistry.getConfigurationListeners().isEmpty()) {
				// We know we have advisors that may affect this object
				// Prepare to proxy it
				ProxyFactory pf;
				if (shouldProxyBeanCreationMethod(m)) {
					pf = new ProxyFactory(originallyCreatedBean);
					pf.setProxyTargetClass(true);
				}
				else {
					pf = new ProxyFactory(new Class[] { m.getReturnType() });
					pf.setProxyTargetClass(false);
					pf.setTarget(originallyCreatedBean);
				}

				boolean customized = false;
				for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
					customized = customized
							|| cml.processBeanMethodReturnValue(owningBeanFactory, childTrackingFactory,
								originallyCreatedBean, m, pf);
				}

				// Only proxy if we know that advisors apply to this bean
				if (customized || pf.getAdvisors().length > 0) {
					// Ensure that AspectJ pointcuts will work
					pf.addAdvice(0, ExposeInvocationInterceptor.INSTANCE);
					if (pf.isProxyTargetClass() && Modifier.isFinal(m.getReturnType().getModifiers())) {
						throw new BeanDefinitionStoreException(m + " is eligible for proxying target class "
								+ "but return type " + m.getReturnType().getName() + " is final");
					}
					return pf.getProxy();
				}
				else {
					return originallyCreatedBean;
				}
			}
			else {
				// There can be no advisors
				return originallyCreatedBean;
			}
		}
		finally {
			if (lastRequestedBeanName == null) {
				childTrackingFactory.pop();
			}
		}
	}

	/**
	 * Use CGLIB only if the return type isn't an interface
	 * 
	 * @param m
	 * @param c
	 * @return
	 */
	private boolean shouldProxyBeanCreationMethod(Method m) {
		Bean bean = AnnotationUtils.findAnnotation(m, Bean.class);

		// TODO need to consider autowiring enabled at factory level
		return !m.getReturnType().isInterface() || bean.autowire().isAutowire();
	}
}
