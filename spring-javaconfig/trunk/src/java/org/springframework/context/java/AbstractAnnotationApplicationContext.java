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
package org.springframework.context.java;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.java.ConfigurationPostProcessor;
import org.springframework.beans.factory.support.ConfigurationClassScanningBeanDefinitionReader;
import org.springframework.beans.factory.support.AbstractClassScanningBeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Convenient superclass for ApplicationContext implementations that reads bean
 * definitions from available classes.
 * 
 * <p>
 * This class registers each bean definition with the DefaultListableBeanFactory
 * superclass, and relies on the latter's implementation of the BeanFactory
 * interface. It supports singletons, prototypes, and references to either of
 * these kinds of bean.
 * 
 * @author Costin Leau
 * 
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
public abstract class AbstractAnnotationApplicationContext extends AbstractRefreshableApplicationContext {

	/**
	 * Register the default post processors used for parsing Spring classes.
	 * 
	 */
	protected void registerDefaultPostProcessors() {
		addBeanFactoryPostProcessor(new ConfigurationPostProcessor());
	}

	public AbstractAnnotationApplicationContext() {
		this(null);
	}

	public AbstractAnnotationApplicationContext(ApplicationContext parent) {
		super(parent);
		registerDefaultPostProcessors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.support.AbstractRefreshableApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.support.DefaultListableBeanFactory)
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		AbstractClassScanningBeanDefinitionReader reader = createAnnotationBeanDefinitionReader(beanFactory);
		reader.setResourceLoader(this);

		loadBeanDefinitions(reader);

		// treat Class case separately
		loadBeanDefinitions(beanFactory, getConfigClasses());
	}

	protected AbstractClassScanningBeanDefinitionReader createAnnotationBeanDefinitionReader(
			DefaultListableBeanFactory beanFactory) {
		return new ConfigurationClassScanningBeanDefinitionReader(beanFactory);
	}

	/**
	 * Load bean definitions with the given
	 * AbstractAnnotationBeanDefinitionReader.
	 * <p>
	 * The lifecycle of the bean factory is handled by the refreshBeanFactory
	 * method; therefore this method is just supposed to load and/or register
	 * bean definitions.
	 * <p>
	 * Delegates to a ResourcePatternResolver for resolving location patterns
	 * into Resource instances.
	 * 
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException if the required class definition isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(AbstractClassScanningBeanDefinitionReader reader) throws BeansException,
			IOException {
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Load bean definitions from configuration classes.
	 * <p>
	 * Since Class objects cannot be easily translated into a byte array or
	 * InputStream, they have be parsed separately.
	 * 
	 * @param configClasses
	 */
	protected int loadBeanDefinitions(DefaultListableBeanFactory beanFactory, Class...configClasses) {
		int loadedDefs = 0;
		if (configClasses != null) {
			for (Class clazz : configClasses) {
				if (containsConfiguration(clazz)) {
					loadedDefs++;
					beanFactory.registerBeanDefinition(clazz.getName(), new RootBeanDefinition(clazz));
				}
			}
		}

		return loadedDefs;
	}

	/**
	 * Discriminator between configuration and non-configuration classes.
	 * 
	 * @param clazz
	 * @return
	 */
	protected boolean containsConfiguration(Class<?> clazz) {
		return clazz.isAnnotationPresent(Configuration.class);
	}

	/**
	 * Return an array of Resource objects, referring to the class bean
	 * definition files that this context should be built with.
	 * <p>
	 * Default implementation returns <code>null</code>. Subclasses can
	 * override this to provide pre-built Resource objects rather than location
	 * Strings.
	 * 
	 * @return an array of Resource objects, or <code>null</code> if none
	 * @see #getConfigLocations()
	 */
	protected Resource[] getConfigResources() {
		return null;
	}

	/**
	 * Return an array of resource locations, referring to the class bean
	 * definition files that this context should be built with. Can also include
	 * location patterns, which will get resolved via a ResourcePatternResolver.
	 * <p>
	 * Default implementation returns <code>null</code>. Subclasses can
	 * override this to provide a set of resource locations to load bean
	 * definitions from.
	 * 
	 * @return an array of resource locations, or <code>null</code> if none
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected String[] getConfigLocations() {
		return null;
	}

	/**
	 * Return an array of Class objects which act as definition files for a
	 * spring context.
	 * <p>
	 * Default implementation returns <code>null</code>. Subclasses can
	 * override this to provide pre-built Class objects rather than location
	 * Strings or Resources. This class should be mainly use when runtime
	 * generating classes are used (runtime compiled Groovy scripts or ASM
	 * 'synthetic' classes).
	 * 
	 * @return an array of Class objects, or <code>null</code> if none
	 * @see #getConfigLocations()
	 */
	protected Class[] getConfigClasses() {
		return null;
	}

}
