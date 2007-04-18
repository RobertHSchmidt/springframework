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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.support.BytecodeConfigurationEnhancer;
import org.springframework.config.java.support.factory.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.util.Assert;

/**
 * CGLib bytecode enhancer.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 */
public class CglibConfigurationEnhancer implements BytecodeConfigurationEnhancer {

	/**
	 * 
	 * Intercept only bean creation methods.
	 * 
	 * @author Rod Johnson
	 * 
	 */
	private static class BeanCreationCallbackFilter implements CallbackFilter {

		public int accept(Method m) {
			// We don't intercept non-public methods like finalize
			if (!Modifier.isPublic(m.getModifiers())) {
				return CglibConfigurationEnhancer.NO_OP_CALLBACK_INDEX;
			}
			if (ClassUtils.hasAnnotation(m, Bean.class)) {
				return CglibConfigurationEnhancer.BEAN_CALLBACK_INDEX;
			}
			if (ClassUtils.hasAnnotation(m, ExternalBean.class) || ClassUtils.hasAnnotation(m, AutoBean.class)) {
				return CglibConfigurationEnhancer.EXTERNAL_BEAN_CALLBACK_INDEX;
			}
			return CglibConfigurationEnhancer.NO_OP_CALLBACK_INDEX;
		}
	}

	private static final CallbackFilter BEAN_CREATION_METHOD_CALLBACK_FILTER = new BeanCreationCallbackFilter();

	private static final Class[] CALLBACK_TYPES = new Class[] { NoOp.class, BeanMethodMethodInterceptor.class,
			ExternalBeanMethodMethodInterceptor.class };

	private static final int NO_OP_CALLBACK_INDEX = 0;

	private static final int BEAN_CALLBACK_INDEX = 1;

	private static final int EXTERNAL_BEAN_CALLBACK_INDEX = 2;

	private final ConfigurableListableBeanFactory owningBeanFactory;

	private final ConfigurationListenerRegistry configurationListenerRegistry;

	private final BeanNameTrackingDefaultListableBeanFactory childFactory;

	private BeanNamingStrategy namingStrategy = new MethodNameStrategy();

	public CglibConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory,
			ConfigurationListenerRegistry configurationListenerRegistry) {

		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(configurationListenerRegistry, "configurationListenerRegistry is required");
		this.owningBeanFactory = owningBeanFactory;
		this.configurationListenerRegistry = configurationListenerRegistry;
		this.childFactory = childFactory;
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

		// create callbacks for this particular configuration class to preserve
		// settings (such as naming strategy)

		BeanMethodMethodInterceptor beanMethodCallback = new BeanMethodMethodInterceptor(owningBeanFactory,
				childFactory, configurationListenerRegistry);
		beanMethodCallback.setNamingStrategy(namingStrategy);

		Callback externalBeanCallback = new ExternalBeanMethodMethodInterceptor(owningBeanFactory, namingStrategy);

		Enhancer.registerCallbacks(configurationSubclass, new Callback[] { NoOp.INSTANCE, beanMethodCallback,
				externalBeanCallback });

		return configurationSubclass.asSubclass(configurationClass);
	}

	public void setBeanNamingStrategy(BeanNamingStrategy beanNamingStrategy) {
		this.namingStrategy = beanNamingStrategy;
	}

}
