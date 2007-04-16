/*
 * Copyright 2002-2006 the original author or authors.
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
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.DependencyCheck;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.Scope;
import org.springframework.config.java.listener.ConfigurationListener;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.process.naming.BeanNamingStrategy;
import org.springframework.config.java.process.naming.ChainedStrategy;
import org.springframework.config.java.support.BytecodeConfigurationEnhancer;
import org.springframework.config.java.support.cglib.CglibConfigurationEnhancer;
import org.springframework.config.java.support.factory.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * Class that processes Configuration beans.
 * <p>
 * A Configuration bean contains bean definition methods annotated with the Bean
 * annotation. The Configuration class itself may optionally be annotated with a
 * Configuration annotation setting global defaults.
 * <p>
 * Bean creation methods may be public or protected.
 * <p>
 * Typically used for only one configuration class in a single
 * BeanDefinitionRegistry.
 * <p>
 * Most of the actual work is performed by ConfigurationListeners, which makes
 * the processing of this class extensible. ConfigurationListeners react to
 * configuration methods and classes.
 * 
 * @author Rod Johnson
 * @see org.springframework.config.java.listener.ConfigurationListener
 */
public class ConfigurationProcessor {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Bean factory that this post processor runs in
	 */
	private ConfigurableListableBeanFactory owningBeanFactory;

	/**
	 * Used to hold Spring AOP advisors and other internal objects while
	 * processing configuration. Object added to this factory can still benefit
	 * from autowiring and other IoC container features, but are not visible
	 * externally.
	 */
	private BeanNameTrackingDefaultListableBeanFactory childFactory;

	private ConfigurationListenerRegistry configurationListenerRegistry;

	private BytecodeConfigurationEnhancer configurationEnhancer;

	private BeanNamingStrategy beanNamingStrategy;

	public ConfigurationProcessor(ConfigurableApplicationContext ac, ConfigurationListenerRegistry clr) {
		init(ac.getBeanFactory(), clr);
	}

	/**
	 * Create a configuration processor. This is tied to an owning factory.
	 * 
	 * @param bdr owning factory
	 */
	public ConfigurationProcessor(ConfigurableListableBeanFactory bdr, ConfigurationListenerRegistry clr) {
		init(bdr, clr);
	}

	protected void init(ConfigurableListableBeanFactory bdr, ConfigurationListenerRegistry clr) {
		this.owningBeanFactory = bdr;
		this.configurationListenerRegistry = clr;
		this.childFactory = new BeanNameTrackingDefaultListableBeanFactory(owningBeanFactory);
		// TODO: this should be pluggable
		this.configurationEnhancer = new CglibConfigurationEnhancer(bdr, childFactory, configurationListenerRegistry);

		// default naming strategy
		if (this.beanNamingStrategy == null)
			this.beanNamingStrategy = new ChainedStrategy();
	}

	/**
	 * Generate bean definitions
	 * 
	 * @param configObject
	 */
	public void process(Class<?> configClass) throws BeanDefinitionStoreException {
		if (!ClassUtils.isConfigurationClass(configClass, configurationListenerRegistry)) {
			throw new BeanDefinitionStoreException(configClass.getName()
					+ " contains no Bean creation methods or Aspect methods");
		}

		if (Modifier.isFinal(configClass.getModifiers())) {
			throw new BeanDefinitionStoreException("Configuration class " + configClass.getName() + " my not be final");
		}

		String configurerBeanName = configClass.getName();

		Class<?> configSubclass = configurationEnhancer.enhanceConfiguration(configClass);
		((DefaultListableBeanFactory) owningBeanFactory).registerBeanDefinition(configurerBeanName,
			new RootBeanDefinition(configSubclass));

		generateBeanDefinitions(configurerBeanName, configClass);
	}

	/**
	 * Modify metadata by emitting new bean definitions based on the bean
	 * creation methods in this Java file
	 * 
	 * @param configurerBeanName name of the bean containing the factory methods
	 * @param configurerClass class of the configurer bean instance
	 */
	public void generateBeanDefinitions(final String configurerBeanName, final Class<?> configurerClass) {

		// Callback listeners
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners()) {
			cl.configurationClass(owningBeanFactory, childFactory, configurerBeanName, configurerClass);
		}

		// Only want to consider most specific bean creation method, in the case
		// of overrides
		final Set<String> noArgMethodsSeen = new HashSet<String>();

		ReflectionUtils.doWithMethods(configurerClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
				if (beanAnnotation != null && !noArgMethodsSeen.contains(m.getName())) {

					// If the bean already exists in the factory, don't emit a
					// bean definition
					// This may or may not be legal, depending on whether the
					// @Bean annotation
					// allows overriding
					if (owningBeanFactory.containsBean(m.getName())) {
						if (!beanAnnotation.allowOverriding()) {
							throw new IllegalStateException("Already have a bean with name '" + m.getName() + "'");
						}
						else {
							// Don't emit a bean definition
							return;
						}
					}
					noArgMethodsSeen.add(m.getName());
					generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory, configurerBeanName,
						configurerClass, m, beanAnnotation);// , cca);
				}
				else {
					for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
						cml.otherMethod(owningBeanFactory, childFactory, configurerBeanName, configurerClass, m);
					}
				}
			}
		});

		// Find inner aspect classes
		// TODO: need to go up tree? ReflectionUtils.doWithClasses
		for (Class innerClass : configurerClass.getDeclaredClasses()) {
			if (Modifier.isStatic(innerClass.getModifiers())) {
				process(innerClass);
			}
		}
	}

	protected void generateBeanDefinitionFromBeanCreationMethod(ConfigurableListableBeanFactory beanFactory,
			String configurerBeanName, Class<?> configurerClass, Method beanCreationMethod, Bean beanAnnotation) {

		if (log.isDebugEnabled())
			log.debug("Found bean creation method " + beanCreationMethod);

		validateBeanCreationMethod(beanCreationMethod);

		// Create a bean definition from the method
		RootBeanDefinition rbd = new RootBeanDefinition(beanCreationMethod.getReturnType());
		rbd.setFactoryBeanName(configurerBeanName);
		rbd.setFactoryMethodName(beanCreationMethod.getName());

		Configuration config = configurerClass.getAnnotation(Configuration.class);
		String beanName = beanNamingStrategy.getBeanName(beanCreationMethod, config);

		if (log.isDebugEnabled())
			log.debug("Creating future bean " + beanName);
		
		copyAttributes(beanName, beanAnnotation, config, rbd, beanFactory);

		rbd.setResourceDescription("Bean creation method " + beanCreationMethod.getName() + " in class "
				+ beanCreationMethod.getDeclaringClass().getName());

		ConfigurationListener.BeanDefinitionRegistration beanDefinitionRegistration = new ConfigurationListener.BeanDefinitionRegistration(
				rbd, beanName);
		beanDefinitionRegistration.hide = !Modifier.isPublic(beanCreationMethod.getModifiers());

		for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
			cml.beanCreationMethod(beanDefinitionRegistration, beanFactory, childFactory, configurerBeanName,
				configurerClass, beanCreationMethod, beanAnnotation);
		}

		// Not currently used
		// addPropertiesIndicatedByGetterInvocations(configurerClass,
		// beanCreationMethod, rbd);

		// TODO allow use of null return value to suppress bean
		if (beanDefinitionRegistration.hide) {
			childFactory.registerBeanDefinition(beanDefinitionRegistration.name, beanDefinitionRegistration.rbd);
		}
		else {
			((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanDefinitionRegistration.name,
				beanDefinitionRegistration.rbd);
		}
	}

	protected void validateBeanCreationMethod(Method beanCreationMethod) throws BeanDefinitionStoreException {
		if (Modifier.isFinal(beanCreationMethod.getModifiers())) {
			throw new BeanDefinitionStoreException("Bean creation method " + beanCreationMethod.getName()
					+ " may not be final");
		}
		if (beanCreationMethod.getReturnType() == Void.TYPE) {
			throw new BeanDefinitionStoreException("Bean creation method " + beanCreationMethod.getName()
					+ " may not have void return");
		}
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
	protected void copyAttributes(String beanName, Bean beanAnnotation, Configuration configuration,
			RootBeanDefinition rbd, ConfigurableListableBeanFactory beanFactory) {

		// singleton/scope
		rbd.setSingleton(beanAnnotation.scope() == Scope.SINGLETON);

		// depends-on
		rbd.setDependsOn(beanAnnotation.dependsOn());

		// aliases
		for (String alias : beanAnnotation.aliases()) {
			beanFactory.registerAlias(beanName, alias);
		}

		// lifecycle methods
		if (StringUtils.hasText(beanAnnotation.initMethodName())) {
			rbd.setInitMethodName(beanAnnotation.initMethodName());
		}

		if (StringUtils.hasText(beanAnnotation.destroyMethodName())) {
			rbd.setDestroyMethodName(beanAnnotation.destroyMethodName());
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

	public BytecodeConfigurationEnhancer getConfigurationEnhancer() {
		return configurationEnhancer;
	}

	/**
	 * Indicate the naming strategy used for creating the bean names during
	 * processing.
	 * 
	 * @param beanNamingStrategy bean naming strategy implementation
	 */
	public void setBeanNamingStrategy(BeanNamingStrategy beanNamingStrategy) {
		this.beanNamingStrategy = beanNamingStrategy;
	}

	/**
	 * Add properties indicated by getter invocations as found by ASM analysis
	 * 
	 * @param configurerClass
	 * @param beanCreationMethod
	 * @param rbd
	 */
	// private void addPropertiesIndicatedByGetterInvocations(Class<?>
	// configurerClass, Method beanCreationMethod, RootBeanDefinition rbd) {
	// try {
	// String pathString = getPathString(configurerClass);
	// System.out.println("pathString=" + pathString);
	// Resource r = resourceLoader.getResource(pathString);
	// System.out.println(r);
	// if (r.exists()) {
	// InputStream is = r.getInputStream();
	// if (is != null) {
	// ClassReader cr = new ClassReader(is);
	// GetterInvocationFindingClassVisitor gifcv = new
	// GetterInvocationFindingClassVisitor();
	// cr.accept(gifcv, false);
	//					
	// // Add additional properties to bean definition based
	// // on invoked getters
	// List<String> gettersInvoked =
	// gifcv.getGetterInvocations().get(beanCreationMethod.getName());
	// if (gettersInvoked != null) {
	// for (String nameOfGetterInvoked : gettersInvoked) {
	// System.err.println(nameOfGetterInvoked);
	// String propertyName =
	// getBeanPropertyNameForMethodName(nameOfGetterInvoked);
	// rbd.getPropertyValues().addPropertyValue(new PropertyValue(propertyName,
	// resolvePropertyValue(beanCreationMethod.getName(), propertyName)));
	// }
	// }
	// }
	// }
	// }
	// catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	//	
	// protected Object resolvePropertyValue(String name, String propertyName) {
	// throw new UnsupportedOperationException("resolve " + name + "." +
	// propertyName);
	// }
	//
	// // TODO must have this somewhere
	// private static String getBeanPropertyNameForMethodName(String methodName)
	// {
	// return Character.toLowerCase(methodName.charAt(3)) +
	// methodName.substring(4);
	// }
	//
	// private String getPathString(Class<?> configurerClass) {
	// String className = configurerClass.getName();
	// className = "classpath:" + className;
	// className = StringUtils.replace(className, ".", "/");
	// return className + ".class";
	// }
}
