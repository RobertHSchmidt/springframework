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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link BeanFactoryPostProcessor} implementation that adds
 * {@link org.springframework.beans.factory.FactoryBean} definitions for every
 * {@link org.springframework.config.java.annotation.Configuration} bean found
 * in the {@link BeanFactory} processed by
 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)}. It also
 * creates a child factory containing pointcuts and advisors required to
 * interpret any aspects.
 * <p>
 * Use this class to 'bootstrap' JavaConfig within XML:
 * 
 * <pre>
 *     &lt;bean class=&quot;org.springframework.config.java.ConfigurationPostProcessor/&gt;
 *     &lt;bean class=&quot;com.foo.config.ConfigurationA/&gt;
 *     &lt;bean class=&quot;com.foo.config.ConfigurationB/&gt;
 * </pre>
 * 
 * where <tt>ConfigurationA</tt> and <tt>ConfigurationB</tt> are both
 * {@link org.springframework.config.java.annotation.Configuration} classes
 * exposing one or more {@link org.springframework.config.java.annotation.Bean}
 * methods.
 * 
 * @see org.springframework.config.java.annotation.Configuration
 * @see org.springframework.config.java.annotation.Bean
 * @see org.springframework.config.java.process.ConfigurationProcessor
 * @see org.springframework.config.java.context.JavaConfigApplicationContext for
 * details about using JavaConfig without XML
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 */
public class ConfigurationPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered, ResourceLoaderAware,
		ApplicationContextAware {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Will be populated with data from external configuration and handed off to
	 * a new {@link ConfigurationProcessor} for each bean in the
	 * {@link BeanFactory} processed by this instance.
	 */
	protected final ProcessingContext processingContext = new ProcessingContext();

	/**
	 * Allows for optional overriding of default
	 * {@link ConfigurationListenerRegistry} implementation
	 */
	protected ConfigurationListenerRegistry configurationListenerRegistry = new ConfigurationListenerRegistry();

	/**
	 * @see #assertFirstRun()
	 */
	private boolean hasRun;

	/**
	 * Returns HIGHEST_PRECEDENCE, guaranteeing execution will occur before any
	 * other {@link BeanFactoryPostProcessor}
	 * 
	 * @see PriorityOrdered
	 */
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	/**
	 * Allows for optional overriding of the default
	 * {@link ConfigurationListenerRegistry} used during processing.
	 * 
	 * @param configurationListenerRegistry The custom implementation
	 * @see ConfigurationProcessor#ConfigurationProcessor(ProcessingContext,
	 * ConfigurationListenerRegistry) for details on default settings
	 */
	public void setConfigurationListenerRegistry(ConfigurationListenerRegistry configurationListenerRegistry) {
		this.configurationListenerRegistry = configurationListenerRegistry;
	}

	/**
	 * Allows for optional overriding of the default {@link BeanNamingStrategy}
	 * to be used when generating bean definitions from {@link Configuration}
	 * beans.
	 * 
	 * @param beanNamingStrategy The custom implementation
	 * @see ProcessingContext#beanNamingStrategy for details on defaults
	 */
	public void setNamingStrategy(BeanNamingStrategy beanNamingStrategy) {
		this.processingContext.beanNamingStrategy = beanNamingStrategy;
	}

	/**
	 * Allows for autowiring of any enclosing {@link ResourceLoader}. If left
	 * unset, a default implementation will be used.
	 * 
	 * @param the enclosing {@ResourceLoader}
	 * @see ProcessingContext#resourceLoader for details on default value
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.processingContext.resourceLoader = resourceLoader;
	}

	/**
	 * Allows for autowiring of any enclosing ApplicationContext. If
	 * <var>context</var> is of type {@link ConfigurableApplicationContext},
	 * it will be used in lieu of the {@link BeanFactory} supplied to
	 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)}.
	 * Otherwise (if it is not of type {@link ConfigurableApplicationContext})
	 * it will be ignored. Configuration processing will happen against the
	 * aforementioned {@link BeanFactory}, as is the default.
	 * 
	 * @param context the enclosing {@link ApplicationContext}
	 */
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if (context instanceof ConfigurableApplicationContext)
			this.processingContext.owningApplicationContext = (ConfigurableApplicationContext) context;
		else
			log.warn(String.format("enclosing ApplicationContext will be ignored during processing: %s is not a %s",
					context.getClass().getSimpleName(), ConfigurableApplicationContext.class.getSimpleName()));
	}

	/**
	 * Processes each BeanDefinition in <var>beanFactory</var>, looking for any
	 * beans that conform to the semantics of a {@link Configuration}. For any
	 * {@link Configuration} bean found, process that class, generating a
	 * {@link BeanDefinition} for each {@link Bean}-annotated method
	 * encountered.
	 * 
	 * @param beanFactory the enclosing {@link BeanFactory} to be processed
	 * @throws IllegalStateException if this instance has already processed a
	 * {@link BeanFactory}
	 * @see #doPostProcessBeanFactory(ConfigurableListableBeanFactory) for
	 * information on overriding
	 */
	public final void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		assertFirstRun();

		processingContext.owningBeanFactory = beanFactory;

		doPostProcessBeanFactory(beanFactory);
	}

	/**
	 * Allows for overriding core logic for processing each bean in
	 * <var>beanFactory</var>. Invariants have been handled by
	 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)}:
	 * <ul>
	 * <li>this is guaranteed to be the first and only run for this instance</li>
	 * <li>processingContext is guaranteed to be ready for handoff to a new
	 * {@link ConfigurationProcessor} instance</li>
	 * </ul>
	 * @param beanFactory the {@link BeanFactory} to process
	 * @see BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
	 * for details on the lifecycle of BeanFactoryPostProcessors
	 */
	protected void doPostProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		String[] beanNames = beanFactory.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			Class<?> clazz = getBeanClass(beanName, beanFactory);
			if (configurationListenerRegistry.isConfigurationClass(clazz)) {
				ConfigurationProcessor processor = new ConfigurationProcessor(processingContext,
						configurationListenerRegistry);
				processor.processConfigurationBean(beanName, clazz);

			}
		}

	}

	/**
	 * Validates that this instance processes one and only one BeanFactory
	 * @throws IllegalStateException if this instance has already processed a
	 * {@link BeanFactory}
	 */
	private void assertFirstRun() throws IllegalStateException {
		if (hasRun)
			throw new IllegalStateException("A ConfigurationPostProcessor cannot run on two BeanFactories");
		hasRun = true;
	}

	/**
	 * Queries <var>beanFactory</var> to determine the class associated with
	 * <var>beanName</var> Will return null if no bean is not valid or if no
	 * class can be found. Will throw an exception if the found class is
	 * invalid.
	 * 
	 * No check for {@link Bean} or {@link Configuration} annotations is made.
	 * 
	 * @param beanName target bean
	 * @param beanFactory {@link BeanFactory} currently being processed
	 * 
	 * @return class associated with <var>beanName</var>, null if that class is
	 * invalid, e.g.: abstract or does not specify a class attribute
	 */
	private Class<?> getBeanClass(String beanName, ConfigurableListableBeanFactory beanFactory) {

		BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);

		// is the bean definition eligible? Must be non-abstract and specify a
		// class
		if (beanDef.isAbstract() || (!StringUtils.hasText(beanDef.getBeanClassName())))
			return null;

		// required for updating the bean class
		AbstractBeanDefinition definition = (AbstractBeanDefinition) beanDef;

		// TODO: check for FactoryBean/factory-method type of beans
		// hard since we are a BFPP and it's impossible to get the actual
		// configuration instance/class w/o initilizing the factory-method/FB
		// even for non @Configuration cases.

		if (definition.hasBeanClass())
			return definition.getBeanClass();

		// load the class (changes in the lazy loading code part of
		// spring core)

		// TODO: add support for factory-method beans (and other not-normal
		// beans) this requires transforming the BFPP into a BPP and might
		// require multiple instantion of the configuration class.
		if (beanDef.getBeanClassName() != null) {
			try {
				return ClassUtils.forName(beanDef.getBeanClassName());
			}
			catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Bean class '" + beanDef.getBeanClassName() + "' not found");
			}
		}

		return null;
	}

}
