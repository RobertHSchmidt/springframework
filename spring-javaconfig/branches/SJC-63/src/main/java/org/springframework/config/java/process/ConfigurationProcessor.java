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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.core.BeanMethodReturnValueProcessor;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.core.ExternalBeanMethodProcessor;
import org.springframework.config.java.core.ExternalValueMethodProcessor;
import org.springframework.config.java.core.StandardBeanMethodProcessor;
import org.springframework.config.java.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.enhancement.cglib.CglibConfigurationEnhancer;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
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
 * <p>
 * This class implements {@link InitializingBean} interface - remember to call
 * {@link InitializingBean#afterPropertiesSet()} before any processing.
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 * @see org.springframework.config.java.process.ConfigurationListener
 */
public class ConfigurationProcessor implements Reactor, InitializingBean, ResourceLoaderAware {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Bean factory that this post processor runs in
	 */
	// TODO: make final once again. Currently ImportConfigurationListener is
	// using this.
	final ConfigurableListableBeanFactory owningBeanFactory;

	/**
	 * Used to hold Spring AOP advisors and other internal objects while
	 * processing configuration. Object added to this factory can still benefit
	 * from autowiring and other IoC container features, but are not visible
	 * externally.
	 */
	private final BeanNameTrackingDefaultListableBeanFactory childFactory;

	/**
	 * Non-null if we are running in an ApplicationContext and need to ensure
	 * that BeanFactoryProcessors from the parent apply to the child
	 */
	private AbstractApplicationContext owningApplicationContext;

	/**
	 * If we are running in an ApplicationContext we create a child context, as
	 * well as a child BeanFactory, so that we can apply
	 * BeanFactoryPostProcessors to the child
	 */
	private ConfigurableApplicationContext childApplicationContext;

	private ConfigurationListenerRegistry configurationListenerRegistry = new ConfigurationListenerRegistry();

	private ConfigurationEnhancer configurationEnhancer;

	private BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

	private CompositeValueSource valueSource = new CompositeValueSource();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private boolean initialized = false;

	/**
	 * Constructor taking an application context as parameter. Suitable for
	 * programmatic use.
	 * 
	 * @param ac application context in which the newly created bean definition
	 * will reside
	 */
	public ConfigurationProcessor(ConfigurableApplicationContext ac) {
		this(ac.getBeanFactory());

		if (ac instanceof AbstractApplicationContext) {
			owningApplicationContext = (AbstractApplicationContext) ac;
			childApplicationContext = createChildApplicationContext();
			copyBeanPostProcessors(owningApplicationContext, childApplicationContext);
		}
	}

	private ConfigurableApplicationContext createChildApplicationContext() {
		ConfigurableApplicationContext child = new GenericApplicationContext(childFactory, owningApplicationContext) {
			// TODO this override is a hack! Why is EventMulticaster null?
			@Override
			public void publishEvent(ApplicationEvent event) {
				if (event instanceof ContextRefreshedEvent)
					log.debug("suppressed " + event);
			}
		};

		// Piggyback on owning application context refresh
		owningApplicationContext.addApplicationListener(new ApplicationListener() {
			public void onApplicationEvent(ApplicationEvent event) {
				if (event instanceof ContextRefreshedEvent)
					ConfigurationProcessor.this.childApplicationContext.refresh();
			}
		});

		return child;
	}

	private void copyBeanPostProcessors(AbstractApplicationContext source, ConfigurableApplicationContext dest) {
		// TODO: why do both of the iterations below? wouldn't one of the
		// two suffice?
		List<BeanFactoryPostProcessor> bfpps = new LinkedList<BeanFactoryPostProcessor>();
		for (Object o : source.getBeansOfType(BeanFactoryPostProcessor.class).values())
			if (!(o instanceof ConfigurationPostProcessor))
				bfpps.add((BeanFactoryPostProcessor) o);

		for (Object o : source.getBeanFactoryPostProcessors())
			if (!(o instanceof ConfigurationPostProcessor))
				bfpps.add((BeanFactoryPostProcessor) o);

		// Add all BeanFactoryPostProcessors to the child context
		for (BeanFactoryPostProcessor bfpp : bfpps)
			dest.addBeanFactoryPostProcessor(bfpp);
	}

	/**
	 * Create a configuration processor. This is tied to an owning factory.
	 * 
	 * @param clbf owning factory
	 */
	public ConfigurationProcessor(ConfigurableListableBeanFactory clbf) {
		this.owningBeanFactory = clbf;
		this.childFactory = new BeanNameTrackingDefaultListableBeanFactory(owningBeanFactory);
	}

	/**
	 * Optionally indicate the resourceLoader. If unspecified,
	 * {@link DefaultResourceLoader} will be used. will be used.
	 * 
	 * @param resourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Optionally indicate the naming strategy used for creating the bean names
	 * during processing. If unspecified, {@link BeanNamingStrategy} will be
	 * used.
	 * 
	 * @param beanNamingStrategy bean naming strategy implementation
	 */
	public void setBeanNamingStrategy(BeanNamingStrategy beanNamingStrategy) {
		this.beanNamingStrategy = beanNamingStrategy;
	}

	/**
	 * Optionally specify a custom subclass of
	 * {@link ConfigurationListenerRegistry}. If unspecified, the default
	 * implementation will be used.
	 * 
	 * <p/>TODO: if ConfigurationListener is made internal, this configuration
	 * should be eliminated as well
	 * 
	 * @param configurationListenerRegistry
	 */
	public void setConfigurationListenerRegistry(ConfigurationListenerRegistry configurationListenerRegistry) {
		this.configurationListenerRegistry = configurationListenerRegistry;
	}

	public void addValueSource(ValueSource vs) {
		this.valueSource.add(vs);
	}

	public BeanFactory getChildBeanFactory() {
		return (childApplicationContext != null) ? childApplicationContext : childFactory;
	}

	public void registerBeanDefinition(String name, BeanDefinition bd, boolean hide) {
		if (hide)
			childFactory.registerBeanDefinition(name, bd);
		else
			((BeanDefinitionRegistry) owningBeanFactory).registerBeanDefinition(name, bd);
	}

	public void registerSingleton(String name, Object o, boolean hide) {
		if (hide)
			childFactory.registerSingleton(name, o);
		else
			owningBeanFactory.registerSingleton(name, o);
	}

	/**
	 * Called to avoid constructor changes every time a new configuration switch
	 * appears on this class.
	 */
	public void afterPropertiesSet() {
		Assert.notNull(owningBeanFactory, "an owning factory bean is required");

		ArrayList<BeanMethodReturnValueProcessor> a = new ArrayList<BeanMethodReturnValueProcessor>();
		for (ConfigurationListener c : configurationListenerRegistry.getConfigurationListeners()) {
			a.add(c);
		}

		this.configurationEnhancer = new CglibConfigurationEnhancer(owningBeanFactory, childFactory,
				beanNamingStrategy, a, valueSource);

		initialized = true;
	}

	private void checkInit() {
		if (!initialized) {
			afterPropertiesSet();
		}
	}

	/**
	 * Generate bean definitions from a 'naked' configuration class.
	 * 
	 * <p/> Normally this method is used internally on inner classes however, it
	 * is possible to use it directly on classes that haven't been manually
	 * declared in the enclosing bean factory.
	 * 
	 * @param configurationClass class containing &#64;Configurable or &#64;Bean
	 * annotation
	 * @return the number of bean definition generated (including the
	 * configuration)
	 * @throws BeanDefinitionStoreException if no bean definitions are found
	 */
	public int processClass(Class<?> configurationClass) throws BeanDefinitionStoreException {
		boolean isEntryPoint = false;
		if (beanDefsGenerated == -1) {
			isEntryPoint = true;
			beanDefsGenerated = 0;
		}

		checkInit();

		if (!ConfigurationProcessor.isConfigurationClass(configurationClass, configurationListenerRegistry))
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

		try {
			processConfigurationBean(configBeanName, configurationClass);
			// include the configuration bean definition
			return ++beanDefsGenerated;
		}
		finally {
			if (isEntryPoint)
				beanDefsGenerated = -1;
		}
	}

	int beanDefsGenerated = -1;

	/**
	 * Primary point of entry used by {@link ConfigurationPostProcessor}.
	 * 
	 * @param configurationBeanName
	 * @return
	 * @throws BeanDefinitionStoreException
	 */
	void processConfigurationBean(String configurationBeanName, Class<?> configurationClass) {
		boolean isEntryPoint = false;
		if (beanDefsGenerated == -1) {
			isEntryPoint = true;
			beanDefsGenerated = 0;
		}

		try {
			checkInit();

			Assert.notNull(configurationBeanName, "beanName is required");
			Assert.notNull(configurationClass, "configurationClass is required");

			enhanceConfigurationClassAndUpdateBeanDefinition(configurationClass, configurationBeanName);

			generateBeanDefinitions(configurationBeanName, configurationClass);

			return;
		}
		finally {
			if (isEntryPoint)
				beanDefsGenerated = -1;
		}
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
	protected void generateBeanDefinitions(String configurationBeanName, Class<?> configurationClass) {

		sourceClassEvent(new ClassEvent(this, configurationClass));
		processMethods(configurationBeanName, configurationClass);

	}

	private void processMethods(final String configurationBeanName, final Class<?> configurationClass) {

		ReflectionUtils.doWithMethods(configurationClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				Reactor reactor = ConfigurationProcessor.this;
				MethodEvent event = new MethodEvent(reactor, configurationClass, m);
				event.beanNamingStrategy = beanNamingStrategy;
				event.owningBeanFactory = owningBeanFactory;
				event.configurationBeanName = configurationBeanName;
				event.childFactory = childFactory;

				for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners())
					cml.handleEvent(reactor, event);
			}
		}, new ReflectionUtils.MethodFilter() {
			public boolean matches(Method candidateMethod) {
				return !candidateMethod.getDeclaringClass().equals(Object.class);
			}
		});
	}

	private void enhanceConfigurationClassAndUpdateBeanDefinition(Class<?> configurationClass,
			String configurationBeanName) {
		AbstractBeanDefinition definition = (AbstractBeanDefinition) owningBeanFactory
				.getBeanDefinition(configurationBeanName);

		// update the configuration bean definition first
		Class<?> enhancedClass = configurationEnhancer.enhanceConfiguration(configurationClass);
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

	/**
	 * Check if the given class is a configuration.
	 * 
	 * @param candidateConfigurationClass - must be non-abstract and be
	 * annotated with &#64;Configuration and/or have at least one method
	 * annotated with &#64;Bean
	 */
	public static boolean isConfigurationClass(Class<?> candidateConfigurationClass) {
		Assert.notNull(candidateConfigurationClass);

		if (Modifier.isAbstract(candidateConfigurationClass.getModifiers())
				&& ExternalBeanMethodProcessor.findExternalBeanCreationMethods(candidateConfigurationClass).isEmpty()
				&& ExternalValueMethodProcessor.findExternalValueCreationMethods(candidateConfigurationClass).isEmpty())
			return false;

		return candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !StandardBeanMethodProcessor.findBeanCreationMethods(candidateConfigurationClass).isEmpty();
	}

	/**
	 * Check if the given class is a configuration.
	 * 
	 * Additionally, a listener registry is checked against the class.
	 * 
	 * @param candidateConfigurationClass
	 * @param registry
	 * @return
	 */
	static boolean isConfigurationClass(Class<?> candidateConfigurationClass, ConfigurationListenerRegistry registry) {

		if (isConfigurationClass(candidateConfigurationClass)) {
			CglibConfigurationEnhancer.validateSuitabilityForEnhancement(candidateConfigurationClass);
			return true;
		}

		if (registry != null)
			for (ConfigurationListener cl : registry.getConfigurationListeners())
				if (cl.understands(candidateConfigurationClass))
					return true;

		return false;
	}

	public void sourceEvent(Event event) {
		throw new UnsupportedOperationException();
		/*
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners())
			cl.handleEvent(this, event);
		*/
	}

	public void sourceClassEvent(ClassEvent event) {

		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners())
			if (cl.understands(event.clazz))
				cl.handleEvent(this, event);

	}

	public void sourceBeanMethodEvent(BeanMethodEvent event) {
		for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners())
			cml.handleEvent(this, event);
	}

}
