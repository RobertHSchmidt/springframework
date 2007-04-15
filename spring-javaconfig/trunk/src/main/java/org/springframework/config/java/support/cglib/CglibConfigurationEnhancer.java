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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.support.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.support.BytecodeConfigurationEnhancer;
import org.springframework.config.java.support.ConfigurationListenerRegistry;
import org.springframework.util.Assert;

/**
 * CGLib bytecode enhancer.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 */
public class CglibConfigurationEnhancer implements BytecodeConfigurationEnhancer {

	private static final CallbackFilter BEAN_CREATION_METHOD_CALLBACK_FILTER = new BeanCreationCallbackFilter();

	private static final Class[] CALLBACK_TYPES = new Class[] { NoOp.class, BeanMethodMethodInterceptor.class,
			ExternalBeanMethodMethodInterceptor.class };

	private final ConfigurableListableBeanFactory owningBeanFactory;

	private final ConfigurationListenerRegistry configurationListenerRegistry;

	private final BeanNameTrackingDefaultListableBeanFactory childFactory;

	private final Callback EXTERNAL_BEAN_CALLBACK_INSTANCE;

	public CglibConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory,
			ConfigurationListenerRegistry configurationListenerRegistry) {
		
		Assert.notNull(owningBeanFactory, "owningBeanFactory is required");
		Assert.notNull(configurationListenerRegistry, "configurationListenerRegistry is required");
		this.owningBeanFactory = owningBeanFactory;
		this.configurationListenerRegistry = configurationListenerRegistry;
		this.childFactory = childFactory;

		EXTERNAL_BEAN_CALLBACK_INSTANCE = new ExternalBeanMethodMethodInterceptor(owningBeanFactory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.support.BytecodeConfigurationEnhancer#enhanceConfiguration(java.lang.Class)
	 */
	public Class enhanceConfiguration(Class configurationClass) {
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

		Class configurationSubclass = enhancer.createClass();

		Enhancer.registerCallbacks(configurationSubclass, new Callback[] { NoOp.INSTANCE,
				// TODO: can this be shared also?
				new BeanMethodMethodInterceptor(owningBeanFactory, childFactory, configurationListenerRegistry),
				EXTERNAL_BEAN_CALLBACK_INSTANCE });

		return configurationSubclass;

	}

}
