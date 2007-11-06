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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.support.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.support.BytecodeConfigurationEnhancer;
import org.springframework.config.java.support.MethodBeanWrapper;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.util.Assert;

/**
 * CGLib bytecode enhancer.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 */
public class CglibConfigurationEnhancer implements BytecodeConfigurationEnhancer {

	private static class BeanCreationCallbackFilter implements CallbackFilter {
		public int accept(Method m) {
			if (ClassUtils.hasAnnotation(m, ScopedProxy.class))
				return CglibConfigurationEnhancer.SCOPED_PROXY_CALLBACK_INDEX;
			if (ClassUtils.hasAnnotation(m, Bean.class)) {
				return CglibConfigurationEnhancer.BEAN_CALLBACK_INDEX;
			}
			if (ClassUtils.hasAnnotation(m, ExternalBean.class) || ClassUtils.hasAnnotation(m, AutoBean.class)) {
				return CglibConfigurationEnhancer.EXTERNAL_BEAN_CALLBACK_INDEX;
			}
			if (ClassUtils.hasAnnotation(m, ExternalValue.class)) {
				return CglibConfigurationEnhancer.EXTERNAL_PROPERTY_CALLBACK_INDEX;
			}
			return CglibConfigurationEnhancer.NO_OP_CALLBACK_INDEX;
		}
	}

	private static final CallbackFilter BEAN_CREATION_METHOD_CALLBACK_FILTER = new BeanCreationCallbackFilter();

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

	private final Callback BEAN_METHOD_CREATION_CALLBACK;

	private final Callback EXTERNAL_BEAN_CALLBACK;

	private final Callback EXTERNAL_PROPERTY_CALLBACK;

	private final Callback SCOPED_PROXY_CALLBACK;

	private final Callback[] CALLBACKS;

	private ConfigurableListableBeanFactory owningBeanFactory;

	private BeanNameTrackingDefaultListableBeanFactory childFactory;

	private BeanNamingStrategy beanNamingStrategy;

	private MethodBeanWrapper beanWrapper;

	public CglibConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, BeanNamingStrategy beanNamingStrategy,
			MethodBeanWrapper beanWrapper, ValueSource valueSource) {

		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(childFactory, "childFactory is required");
		Assert.notNull(beanWrapper, "beanWrapper is required");
		Assert.notNull(beanNamingStrategy, "beanNamingStrategy is required");
		Assert.notNull(valueSource, "valueSource is required");

		this.owningBeanFactory = owningBeanFactory;
		this.childFactory = childFactory;
		this.beanWrapper = beanWrapper;
		this.beanNamingStrategy = beanNamingStrategy;

		BEAN_METHOD_CREATION_CALLBACK = new BeanMethodMethodInterceptor(this.owningBeanFactory, this.childFactory,
				this.beanWrapper, this.beanNamingStrategy);

		EXTERNAL_BEAN_CALLBACK = new ExternalBeanMethodMethodInterceptor(this.owningBeanFactory,
				this.beanNamingStrategy);

		EXTERNAL_PROPERTY_CALLBACK = new ExternalValueMethodMethodInterceptor(valueSource);

		SCOPED_PROXY_CALLBACK = new ScopedProxyBeanMethodMethodInterceptor(
				(BeanMethodMethodInterceptor) BEAN_METHOD_CREATION_CALLBACK);

		CALLBACKS = new Callback[] { NoOp.INSTANCE, BEAN_METHOD_CREATION_CALLBACK, EXTERNAL_BEAN_CALLBACK,
				SCOPED_PROXY_CALLBACK, EXTERNAL_PROPERTY_CALLBACK };
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.support.BytecodeConfigurationEnhancer#enhanceConfiguration(java.lang.Object,
	 * java.lang.Class)
	 */
	public <T> Class<? extends T> enhanceConfiguration(Class<T> configurationClass) {
		Assert.notNull(configurationClass, "configuration class required");

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(configurationClass);
		enhancer.setUseFactory(false);
		// add the callbacks
		enhancer.setCallbackFilter(BEAN_CREATION_METHOD_CALLBACK_FILTER);
		enhancer.setCallbackTypes(CALLBACK_TYPES);

		// TODO can we generate a method to expose each private bean field here?
		// Otherwise may need to generate a static or instance map, with
		// multiple get() methods
		// Listeners don't get callback on this also

		Class<?> configurationSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(configurationSubclass, CALLBACKS);

		return configurationSubclass.asSubclass(configurationClass);
	}
}
