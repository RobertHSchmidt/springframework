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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.enhancement.cglib.CglibConfigurationEnhancer;
import org.springframework.context.ConfigurableApplicationContext;

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
public final class ConfigurationProcessor implements Reactor {

	private final Log log = LogFactory.getLog(getClass());

	private final ProcessingContext processingContext;

	private final ConfigurationListenerRegistry configurationListenerRegistry;

	public ConfigurationProcessor(ConfigurableApplicationContext owningApplicationContext) {
		this(null, null, null, nonNull(owningApplicationContext, "owningApplicationContext"));
	}

	/**
	 * Create a configuration processor. This is tied to an owning factory.
	 * 
	 * @param clbf owning factory
	 */
	public ConfigurationProcessor(ConfigurableListableBeanFactory clbf) {
		this(null, null, nonNull(clbf, "beanFactory"), null);
	}

	/**
	 * @param pc non-null
	 * @param configurationListenerRegistry will default to instantiating new
	 * {@link ConfigurationListenerRegistry} instance if null
	 */
	public ConfigurationProcessor(ProcessingContext pc, ConfigurationListenerRegistry configurationListenerRegistry) {
		this(nonNull(pc, "processingContext"), configurationListenerRegistry, null, null);
	}

	private ConfigurationProcessor(ProcessingContext pc, ConfigurationListenerRegistry configurationListenerRegistry,
			ConfigurableListableBeanFactory owningBeanFactory, ConfigurableApplicationContext owningApplicationContext) {
		this.processingContext = pc != null ? pc : new ProcessingContext();

		if (configurationListenerRegistry != null) {
			this.configurationListenerRegistry = configurationListenerRegistry;
		}
		else {
			log.debug("received null ConfigurationListenerRegistry during construction; will use default");
			this.configurationListenerRegistry = new ConfigurationListenerRegistry();
		}

		processingContext.initialize(owningBeanFactory, owningApplicationContext, this.configurationListenerRegistry);
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
		ClassEvent event = new ClassEvent(this, configurationClass, processingContext);
		/* TODO: SJC-63
		sourceClassEvent(event);
		*/
		new ClassConfigurationListener().handleEvent(this, event);

		try {
			return processingContext.beanDefsGenerated;
		}
		finally {
			processingContext.beanDefsGenerated = 0;
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
		ClassEvent event = new ClassEvent(this, configurationClass, processingContext);
		event.configurationBeanName = configurationBeanName;
		/* TODO: SJC-63
		sourceClassEvent(event);
		*/
		new ClassConfigurationListener().doProcessConfigurationBean(this, configurationBeanName, configurationClass,
				processingContext);
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

	public boolean isConfigClass(Class<?> candidateClass) {
		return candidateClass != null && isConfigurationClass(candidateClass, configurationListenerRegistry);
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

	private static <T> T nonNull(T object, String objectName) {
		if (object == null)
			throw new IllegalArgumentException(objectName + " must be non-null");
		return object;
	}

}
