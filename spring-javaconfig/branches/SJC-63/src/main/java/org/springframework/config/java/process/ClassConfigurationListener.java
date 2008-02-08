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
package org.springframework.config.java.process;

import java.lang.reflect.Method;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

class ClassConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class<?> configurationClass) {
		return ConfigurationUtils.isConfigurationClass(configurationClass);
	}

	public void handleEvent(Reactor reactor, ClassEvent event) {

		if (event.source == this)
			return;

		Class<?> configurationClass = event.clazz;
		ProcessingContext pc = getProcessingContext();

		if (!reactor.isConfigClass(configurationClass))
			return;

		// register the configuration as a bean to allow Spring to use it for
		// creating the actual objects
		// a. produce a bean name based on the class name
		final String configBeanName;
		if (event.configurationBeanName != null)
			configBeanName = event.configurationBeanName;
		else
			configBeanName = configurationClass.getName();

		// create a bean from the configuration class/instance
		RootBeanDefinition configurationBeanDefinition = new RootBeanDefinition();

		// the class enhancement is done by #generateBeanDefinition along with
		// validation
		configurationBeanDefinition.setBeanClass(configurationClass);
		configurationBeanDefinition.setResourceDescription("class-based configuration bean definition");

		Assert.isInstanceOf(DefaultListableBeanFactory.class, pc.owningBeanFactory);

		((DefaultListableBeanFactory) pc.owningBeanFactory).registerBeanDefinition(configBeanName,
				configurationBeanDefinition);

		doProcessConfigurationBean(reactor, configBeanName, configurationClass);
		// include the configuration bean definition
		++pc.beanDefsGenerated;
	}

	void doProcessConfigurationBean(Reactor reactor, String configurationBeanName, Class<?> configurationClass) {
		Assert.notNull(configurationBeanName, "beanName is required");
		Assert.notNull(configurationClass, "configurationClass is required");

		enhanceConfigurationClassAndUpdateBeanDefinition(configurationClass, configurationBeanName);

		generateBeanDefinitions(reactor, configurationBeanName, configurationClass);
	}

	/**
	 * Modify metadata by emitting new bean definitions based on the bean
	 * creation methods in this Java bytecode.
	 * 
	 * @param configurationBeanName name of the bean containing the factory
	 * methods
	 * @param configurationClass enhanced configuration class
	 * @return number of bean definitions created
	 */
	protected void generateBeanDefinitions(Reactor reactor, String configurationBeanName, Class<?> configurationClass) {

		reactor.sourceClassEvent(new ClassEvent(this, configurationClass));
		processMethods(reactor, configurationBeanName, configurationClass);

	}

	private void processMethods(final Reactor reactor, final String configurationBeanName,
			final Class<?> configurationClass) {

		ReflectionUtils.doWithMethods(configurationClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				MethodEvent event = new MethodEvent(reactor, configurationClass, m);
				event.configurationBeanName = configurationBeanName;

				reactor.sourceMethodEvent(event);
			}
		}, new ReflectionUtils.MethodFilter() {
			public boolean matches(Method candidateMethod) {
				return !candidateMethod.getDeclaringClass().equals(Object.class);
			}
		});
	}

	private void enhanceConfigurationClassAndUpdateBeanDefinition(Class<?> configurationClass,
			String configurationBeanName) {
		ProcessingContext pc = getProcessingContext();
		AbstractBeanDefinition definition = (AbstractBeanDefinition) pc.owningBeanFactory
				.getBeanDefinition(configurationBeanName);

		// update the configuration bean definition first
		Class<?> enhancedClass = pc.configurationEnhancer.enhanceConfiguration(configurationClass);
		definition.setBeanClass(enhancedClass);

		// Force resolution of dependencies on other beans
		// It's questionable why this is needed. SPR-33
		for (PropertyValue pv : definition.getPropertyValues().getPropertyValues()) {
			if (pv.getValue() instanceof RuntimeBeanReference) {
				RuntimeBeanReference rbref = (RuntimeBeanReference) pv.getValue();
				String beanName = rbref.getBeanName();
				if (definition.getDependsOn() == null) {
					definition.setDependsOn(new String[] { beanName });
				}
				else {
					String[] added = (String[]) ObjectUtils.addObjectToArray(definition.getDependsOn(), beanName);
					definition.setDependsOn(added);
				}
			}
		}
	}

}
