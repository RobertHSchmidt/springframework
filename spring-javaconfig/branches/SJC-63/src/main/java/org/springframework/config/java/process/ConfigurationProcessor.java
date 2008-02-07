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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.core.BeanMethodReturnValueProcessor;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.enhancement.cglib.CglibConfigurationEnhancer;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
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
public final class ConfigurationProcessor implements Reactor, InitializingBean, ResourceLoaderAware {

	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Bean factory that this post processor runs in
	 */
	private final ConfigurableListableBeanFactory owningBeanFactory;

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

	private BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

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

		CompositeValueSource vs = new CompositeValueSource();

		ConfigurationEnhancer configurationEnhancer = new CglibConfigurationEnhancer(owningBeanFactory, childFactory,
				beanNamingStrategy, a, vs);

		pc = new ProcessingContext(beanNamingStrategy, owningBeanFactory, childFactory, vs, resourceLoader,
				childApplicationContext, configurationEnhancer);
		ProcessingContext.setCurrentContext(pc);

		initialized = true;
	}

	private ProcessingContext pc;

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
		checkInit();

		boolean isEntryPoint = false;
		if (pc.beanDefsGenerated == -1) {
			isEntryPoint = true;
			pc.beanDefsGenerated = 0;
		}

		ClassEvent event = new ClassEvent(this, configurationClass);
		/* TODO: SJC-63
		sourceClassEvent(event);
		*/
		new ClassConfigurationListener().handleEvent(this, event);

		try {
			return pc.beanDefsGenerated;
		}
		finally {
			if (isEntryPoint)
				pc.beanDefsGenerated = -1;
		}
	}

	/**
	 * Primary point of entry used by {@link ConfigurationPostProcessor}.
	 * 
	 * @param configurationBeanName
	 * @return
	 * @throws BeanDefinitionStoreException
	 */
	void processConfigurationBean(String configurationBeanName, Class<?> configurationClass) {
		checkInit();

		boolean isEntryPoint = false;
		if (pc.beanDefsGenerated == -1) {
			isEntryPoint = true;
			pc.beanDefsGenerated = 0;
		}

		try {
			ClassEvent event = new ClassEvent(this, configurationClass);
			event.configurationBeanName = configurationBeanName;
			/* TODO: SJC-63
			sourceClassEvent(event);
			*/
			new ClassConfigurationListener()
					.doProcessConfigurationBean(this, configurationBeanName, configurationClass);

			return;
		}
		finally {
			if (isEntryPoint)
				pc.beanDefsGenerated = -1;
		}
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

		if (ConfigurationUtils.isConfigurationClass(candidateConfigurationClass)) {
			CglibConfigurationEnhancer.validateSuitabilityForEnhancement(candidateConfigurationClass);
			return true;
		}

		if (registry != null)
			for (ConfigurationListener cl : registry.getConfigurationListeners())
				if (cl.understands(candidateConfigurationClass))
					return true;

		return false;
	}

	public boolean isConfigClass(Class<?> candidateConfigurationClass) {
		return isConfigurationClass(candidateConfigurationClass, configurationListenerRegistry);
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

	public void sourceMethodEvent(MethodEvent event) {
		for (ConfigurationListener cl : configurationListenerRegistry.getConfigurationListeners())
			cl.handleEvent(this, event);
	}

	public void sourceBeanMethodEvent(BeanMethodEvent event) {
		for (ConfigurationListener cml : configurationListenerRegistry.getConfigurationListeners())
			cml.handleEvent(this, event);
	}

}
