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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.listener.ConfigurationListener;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.support.BytecodeConfigurationEnhancer;
import org.springframework.config.java.support.cglib.CglibConfigurationEnhancer;
import org.springframework.config.java.support.factory.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.util.ClassUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
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
 * <p>
 * The process work on configurations created from classes but also object
 * instances.
 * 
 * 
 * @author Rod Johnson
 * @author Costin Leau
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

		// default naming strategy
		if (this.beanNamingStrategy == null)
			this.beanNamingStrategy = new MethodNameStrategy();

		// TODO: this should be pluggable but has to be a prototype since it
		// depends on the childFactory instance
		CglibConfigurationEnhancer enhancer = new CglibConfigurationEnhancer(bdr, childFactory,
				configurationListenerRegistry);
		enhancer.setBeanNamingStrategy(beanNamingStrategy);

		this.configurationEnhancer = enhancer;
	}

	/**
	 * Generate bean definitions from a rough configuration class.
	 * 
	 * <p/> Normally this method is used internally on inner classes however, it
	 * is possible to use it directly on classes that haven't been manually
	 * declared in the enclosing bean factory.
	 * 
	 * @param configurationClass class containing
	 * @Configurable or
	 * @Bean annotation
	 * @throws BeanDefinitionStoreException if no bean definitions are found
	 */
	public int processClass(Class<?> configurationClass) throws BeanDefinitionStoreException {
		if (!ProcessUtils.validateConfigurationClass(configurationClass, configurationListenerRegistry))
			return 0;

		// register the configuration as a bean to allow Spring to use it for
		// creating the actual objects

		// a. produce a bean name based on the class name
		String configBeanName = configurationClass.getName();

		// create a bean from the configuration class/instance
		RootBeanDefinition configurationBeanDefinition = new RootBeanDefinition();

		// the class enhancement is done by #generateBeanDefinition along with
		// validation
		configurationBeanDefinition.setBeanClass(configurationClass);
		configurationBeanDefinition.setResourceDescription("class-based configuration bean definition");

		Assert.isInstanceOf(DefaultListableBeanFactory.class, owningBeanFactory);

		((DefaultListableBeanFactory) owningBeanFactory).registerBeanDefinition(configBeanName,
			configurationBeanDefinition);

		return generateBeanDefinitions(configBeanName, configurationClass);

	}

	public int processBean(String beanName) throws BeanDefinitionStoreException {
		Assert.notNull(beanName, "beanName is required");
		Class<?> clazz = ProcessUtils.getBeanClass(beanName, owningBeanFactory);

		// no class found
		if (clazz == null)
			return 0;

		// otherwise start configuration processing
		return generateBeanDefinitions(beanName, clazz);

	}

	/**
	 * Modify metadata by emitting new bean definitions based on the bean
	 * creation methods in this Java bytecode. Also, updates the configuration
	 * bytecode definition.
	 * 
	 * @param configurationBeanName name of the bean containing the factory
	 * methods
	 * @param configClass class of the configurer bean instance
	 * @return number of bean created
	 */
	protected int generateBeanDefinitions(final String configurationBeanName, final Class<?> configurationClass) {
		if (!ProcessUtils.validateConfigurationClass(configurationClass, configurationListenerRegistry))
			return 0;

		int beansCreated = 0;
		AbstractBeanDefinition definition = (AbstractBeanDefinition) owningBeanFactory.getBeanDefinition(configurationBeanName);

		// update the configuration bean definition first
		definition.setBeanClass(configurationEnhancer.enhanceConfiguration(configurationClass));

		// Callback listeners
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners()) {
			cl.configurationClass(owningBeanFactory, childFactory, configurationBeanName, configurationClass);
		}

		// Only want to consider most specific bean creation method, in the case
		// of overrides
		// contains the beanNames resolved based on the method signature
		final Set<String> noArgMethodsSeen = new HashSet<String>();
		final int[] countFinalReference = new int[] { beansCreated };

		ReflectionUtils.doWithMethods(configurationClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
				// Determine bean name
				String beanName = beanNamingStrategy.getBeanName(m);

				if (beanAnnotation != null && !noArgMethodsSeen.contains(beanName)) {

					// If the bean already exists in the factory, don't emit a
					// bean definition
					// This may or may not be legal, depending on whether the
					// @Bean annotation
					// allows overriding

					if (owningBeanFactory.containsBean(beanName)) {
						if (!beanAnnotation.allowOverriding()) {
							throw new IllegalStateException("Already have a bean with name '" + beanName + "'");
						}
						else {
							// Don't emit a bean definition
							return;
						}
					}
					noArgMethodsSeen.add(beanName);
					countFinalReference[0] += generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory,
						configurationBeanName, configurationClass, beanName, m, beanAnnotation);// ,
					// cca);
				}
				else {
					for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners()) {
						cml.otherMethod(owningBeanFactory, childFactory, configurationBeanName, configurationClass, m);
					}
				}
			}
		});

		beansCreated = countFinalReference[0];

		// Find inner aspect classes
		// TODO: need to go up tree? ReflectionUtils.doWithClasses
		for (Class innerClass : configurationClass.getDeclaredClasses()) {
			if (Modifier.isStatic(innerClass.getModifiers())) {
				beansCreated += processClass(innerClass);
			}
		}

		return beansCreated;
	}

	/**
	 * Generate the actual bean definition using the given method.
	 * 
	 * @param beanFactory containing beanFactory
	 * @param configurerBeanName the configuration name
	 * @param configurerClass configuration class
	 * @param beanCreationMethod method creating the actual bean
	 * @param beanAnnotation the Bean annotation available on the creation
	 * method.
	 */
	protected int generateBeanDefinitionFromBeanCreationMethod(ConfigurableListableBeanFactory beanFactory,
			String configurerBeanName, Class<?> configurerClass, String beanName, Method beanCreationMethod,
			Bean beanAnnotation) {

		int count = 0;
		if (log.isDebugEnabled())
			log.debug("Found bean creation method " + beanCreationMethod);

		ProcessUtils.validateBeanCreationMethod(beanCreationMethod);

		// Create a bean definition from the method

		RootBeanDefinition rbd = new RootBeanDefinition(beanCreationMethod.getReturnType());

		rbd.setFactoryMethodName(beanCreationMethod.getName());
		rbd.setFactoryBeanName(configurerBeanName);
		// tag the bean definition
		rbd.setAttribute(ClassUtils.JAVA_CONFIG_PKG, Boolean.TRUE);

		Configuration config = configurerClass.getAnnotation(Configuration.class);

		if (log.isDebugEnabled())
			log.debug("Creating future bean " + beanName);

		ProcessUtils.copyAttributes(beanName, beanAnnotation, config, rbd, beanFactory);

		// create description string
		StringBuilder builder = new StringBuilder("Bean creation method ");
		builder.append(beanCreationMethod.getName());
		builder.append(" in class ");
		builder.append(beanCreationMethod.getDeclaringClass().getName());

		rbd.setResourceDescription(builder.toString());

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

		return count;
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
