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
package org.springframework.config.java.enhancement;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.core.AutoBeanMethodProcessor;
import org.springframework.config.java.core.BeanMethodReturnValueProcessor;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.core.ExternalBeanMethodProcessor;
import org.springframework.config.java.core.ExternalValueMethodProcessor;
import org.springframework.config.java.core.StandardBeanMethodProcessor;
import org.springframework.config.java.core.ScopedProxyMethodProcessor;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.util.Assert;

/**
 * {@link ConfigurationEnhancer} implementation that uses CGLIB to subclass and
 * enhance a target
 * {@link org.springframework.config.java.annotation.Configuration} class.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 */
class CglibConfigurationEnhancer implements ConfigurationEnhancer {

	private static final CallbackFilter BEAN_CREATION_METHOD_CALLBACK_FILTER = new CallbackFilter() {
		public int accept(Method candidateMethod) {
			if (ScopedProxyMethodProcessor.isCandidate(candidateMethod))
				return SCOPED_PROXY_CALLBACK_INDEX;

			if (StandardBeanMethodProcessor.isCandidate(candidateMethod))
				return BEAN_CALLBACK_INDEX;

			if (ExternalBeanMethodProcessor.isCandidate(candidateMethod)
					|| AutoBeanMethodProcessor.isCandidate(candidateMethod))
				return EXTERNAL_BEAN_CALLBACK_INDEX;

			if (ExternalValueMethodProcessor.isCandidate(candidateMethod))
				return EXTERNAL_PROPERTY_CALLBACK_INDEX;

			return NO_OP_CALLBACK_INDEX;
		}
	};

	private static final Class<?>[] CALLBACK_TYPES = new Class[] { NoOp.class, BeanMethodMethodInterceptor.class,
			ExternalBeanMethodMethodInterceptor.class, ScopedProxyBeanMethodMethodInterceptor.class,
			ExternalValueMethodMethodInterceptor.class };

	// non-intercepted method
	private static final int NO_OP_CALLBACK_INDEX = 0;

	// normal method invocation
	private static final int BEAN_CALLBACK_INDEX = 1;

	// external bean invocation
	private static final int EXTERNAL_BEAN_CALLBACK_INDEX = 2;

	// external property resolution
	private static final int EXTERNAL_PROPERTY_CALLBACK_INDEX = 4;

	// scoped proxy invocation (similar to normal invocation but aware of the
	// scoping prefix)
	private static final int SCOPED_PROXY_CALLBACK_INDEX = 3;

	private final Callback[] callbacks;

	public CglibConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, BeanNamingStrategy beanNamingStrategy,
			List<BeanMethodReturnValueProcessor> returnValueProcessors, ValueSource valueSource) {

		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(childFactory, "childFactory is required");
		Assert.notNull(beanNamingStrategy, "beanNamingStrategy is required");
		Assert.notNull(valueSource, "valueSource is required");

		MethodBeanWrapper beanWrapper = new MethodBeanWrapper(owningBeanFactory, returnValueProcessors, childFactory);

		ExternalValueMethodProcessor evmp = new ExternalValueMethodProcessor(valueSource);
		ExternalBeanMethodProcessor ebmp = new ExternalBeanMethodProcessor(owningBeanFactory, beanNamingStrategy);
		StandardBeanMethodProcessor rbmp = new StandardBeanMethodProcessor(owningBeanFactory, childFactory,
				beanNamingStrategy, beanWrapper);
		ScopedProxyMethodProcessor spmp = new ScopedProxyMethodProcessor(rbmp);

		callbacks = new Callback[] { NoOp.INSTANCE, new BeanMethodMethodInterceptor(rbmp),
				new ExternalBeanMethodMethodInterceptor(ebmp),
				new ScopedProxyBeanMethodMethodInterceptor(spmp, new BeanMethodMethodInterceptor(rbmp)),
				new ExternalValueMethodMethodInterceptor(evmp) };
	}

	public <T> Class<? extends T> enhanceConfiguration(Class<T> configurationClass) {
		Assert.notNull(configurationClass, "configuration class required");

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configurationClass);
		enhancer.setUseFactory(false);
		enhancer.setCallbackFilter(BEAN_CREATION_METHOD_CALLBACK_FILTER);
		enhancer.setCallbackTypes(CALLBACK_TYPES);

		Class<?> configurationSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(configurationSubclass, callbacks);

		return configurationSubclass.asSubclass(configurationClass);
	}
}
