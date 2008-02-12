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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.DependencyCheck;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.Meta;
import org.springframework.config.java.annotation.Primary;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.core.Constants;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.core.StandardBeanMethodProcessor;
import org.springframework.config.java.enhancement.cglib.BeanMethodMethodInterceptor;
import org.springframework.config.java.enhancement.cglib.JavaConfigMethodInterceptor;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * @author Chris Beams
 */
class StandardBeanConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public boolean understands(Class<?> configurationClass) {
		return !(StandardBeanMethodProcessor.findBeanCreationMethods(configurationClass).isEmpty());
	}

	private final HashMap<Class<?>, List<String>> noArgMethodsSeen = new HashMap<Class<?>, List<String>>();

	@Override
	public void handleEvent(Reactor reactor, MethodEvent event) {
		ProcessingContext pc = event.processingContext;
		Method m = event.method;
		Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
		if (beanAnnotation == null)
			return;

		Class<?> configurationClass = event.clazz;
		String configurationBeanName = event.configurationBeanName;

		BeanNamingStrategy beanNamingStrategy = pc.beanNamingStrategy;
		ConfigurableListableBeanFactory owningBeanFactory = pc.owningBeanFactory;
		BeanNameTrackingDefaultListableBeanFactory childFactory = pc.childFactory;

		String beanName = beanNamingStrategy.getBeanName(m);

		if (!noArgMethodsSeen.containsKey(configurationClass))
			noArgMethodsSeen.put(configurationClass, new ArrayList<String>());

		if (noArgMethodsSeen.get(configurationClass).contains(beanName))
			return;

		// If the bean already exists in the factory, don't emit
		// a bean definition. This may or may not be legal,
		// depending on whether the @Bean annotation allows
		// overriding
		if (owningBeanFactory.containsLocalBean(beanName)) {
			if (!beanAnnotation.allowOverriding()) {
				String message = String.format(
						"A bean named '%s' already exists. Consider using @Bean(allowOverriding=true)", beanName);
				throw new IllegalStateException(message);
			}
			// Don't emit a bean definition
			return;
		}
		noArgMethodsSeen.get(configurationClass).add(beanName);

		validateBeanCreationMethod(m);

		// Create a bean definition from the method
		RootBeanDefinition rbd = new RootBeanDefinition(m.getReturnType());

		rbd.setFactoryMethodName(m.getName());
		rbd.setFactoryBeanName(configurationBeanName);
		// tag the bean definition
		rbd.setAttribute(Constants.JAVA_CONFIG_PKG, Boolean.TRUE);

		Configuration config = configurationClass.getAnnotation(Configuration.class);

		copyAttributes(beanName, beanAnnotation, config, rbd, owningBeanFactory);

		// create description string
		StringBuilder builder = new StringBuilder("Bean creation method ");
		builder.append(m.getName());
		builder.append(" in class ");
		builder.append(m.getDeclaringClass().getName());
		rbd.setResourceDescription(builder.toString());

		boolean hide = !Modifier.isPublic(m.getModifiers());

		BeanMethodEvent beanMethodEvent = new BeanMethodEvent(this, configurationClass, m, beanAnnotation, rbd, hide,
				beanName, pc);

		reactor.sourceBeanMethodEvent(beanMethodEvent);

		if (hide) {
			childFactory.registerBeanDefinition(beanName, beanMethodEvent.rbd);
		}
		else {
			((BeanDefinitionRegistry) owningBeanFactory).registerBeanDefinition(beanName, beanMethodEvent.rbd);
		}

		pc.beanDefsGenerated++;
	}

	/**
	 * Validation for the bean creation method. Checks that the method is not
	 * final (so it can be proxied) and that a type is being returned (the
	 * return instance becoming the actual bean).
	 * 
	 * @param beanCreationMethod
	 * @throws BeanDefinitionStoreException
	 */
	private static void validateBeanCreationMethod(Method beanCreationMethod) throws BeanDefinitionStoreException {
		if (Modifier.isFinal(beanCreationMethod.getModifiers()))
			invalidMethod(beanCreationMethod, "may not be final");

		if (Modifier.isPrivate(beanCreationMethod.getModifiers()))
			invalidMethod(beanCreationMethod, "may not be private");

		if (beanCreationMethod.getReturnType() == Void.TYPE)
			invalidMethod(beanCreationMethod, "may not have void return");
	}

	/**
	 * Helper method for {@link #validateBeanCreationMethod(Method)}
	 */
	private static void invalidMethod(Method m, String message) {
		throw new BeanDefinitionStoreException(format("Bean creation method %s.%s %s", m.getDeclaringClass().getName(),
				m.getName(), message));
	}

	/**
	 * Create the bean definition based on the annotation properties.
	 * 
	 * @param beanName name of the bean we're creating (not the factory bean)
	 * @param beanAnnotation bean annotation
	 * @param configuration configuration on the configuration class. Sets
	 * defaults. May be null as this annotation is not required.
	 * @param rbd bean definition, in Spring IoC container internal metadata
	 * @param beanFactory bean factory we are executing in
	 */
	private static void copyAttributes(String beanName, Bean beanAnnotation, Configuration configuration,
			RootBeanDefinition rbd, ConfigurableListableBeanFactory beanFactory) {

		// singleton/scope
		rbd.setScope(beanAnnotation.scope());

		// depends-on
		rbd.setDependsOn(beanAnnotation.dependsOn());

		// aliases
		for (String alias : beanAnnotation.aliases()) {
			beanFactory.registerAlias(beanName, alias);
		}

		// metadata
		for (Meta meta : beanAnnotation.meta()) {
			rbd.setAttribute(meta.name(), meta.value());
		}

		// lifecycle methods
		if (StringUtils.hasText(beanAnnotation.initMethodName())) {
			rbd.setInitMethodName(beanAnnotation.initMethodName());
		}

		if (StringUtils.hasText(beanAnnotation.destroyMethodName())) {
			rbd.setDestroyMethodName(beanAnnotation.destroyMethodName());
		}

		// primary
		if (beanAnnotation.primary() != Primary.UNSPECIFIED) {
			rbd.setPrimary(beanAnnotation.primary().booleanValue());
		}

		// configuration, fallback methods

		if (beanAnnotation.dependencyCheck() != DependencyCheck.UNSPECIFIED) {
			rbd.setDependencyCheck(beanAnnotation.dependencyCheck().value());
		}
		else if (configuration != null && configuration.defaultDependencyCheck() != DependencyCheck.UNSPECIFIED) {
			rbd.setDependencyCheck(configuration.defaultDependencyCheck().value());
		}

		if (beanAnnotation.lazy() != Lazy.UNSPECIFIED) {
			rbd.setLazyInit(beanAnnotation.lazy().booleanValue());
		}
		else if (configuration != null && configuration.defaultLazy() != Lazy.UNSPECIFIED) {
			rbd.setLazyInit(configuration.defaultLazy().booleanValue());
		}

		if (beanAnnotation.autowire() != Autowire.INHERITED) {
			rbd.setAutowireMode(beanAnnotation.autowire().value());
		}
		else if (configuration != null && configuration.defaultAutowire() != Autowire.INHERITED) {
			rbd.setAutowireMode(configuration.defaultAutowire().value());
		}

	}

	@Override
	public JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc) {
		return new BeanMethodMethodInterceptor(pc);
	}
}
