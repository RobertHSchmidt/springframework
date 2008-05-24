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
package org.springframework.config.java.factory.support;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.config.java.core.BeanFactoryFactory;
import org.springframework.config.java.factory.JavaConfigBeanFactory;
import org.springframework.config.java.model.ConfigurationModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class AbstractJavaConfigBeanDefinitionReader implements JavaConfigBeanDefinitionReader {

	private final List<ClassPathResource> aspectClassResources;

	private final ConfigurationModelBeanDefinitionReader modelBeanDefinitionReader;

	protected final JavaConfigBeanFactory beanFactory;

	protected final Log log = LogFactory.getLog(this.getClass());

	@Deprecated
	protected AbstractJavaConfigBeanDefinitionReader(JavaConfigBeanFactory registry) {
		this(registry, null);
	}

	protected AbstractJavaConfigBeanDefinitionReader(JavaConfigBeanFactory registry,
                                                     List<ClassPathResource> aspectClassResources) {
		this.beanFactory = registry;
		this.aspectClassResources = aspectClassResources;
		initializeDeclaringClassBeanFactoryFactory();
		this.modelBeanDefinitionReader = new ConfigurationModelBeanDefinitionReader(beanFactory);
	}

	public int loadBeanDefinitions(Resource[] configClassResources) throws BeanDefinitionStoreException {
		ConfigurationModel model = createConfigurationModel(configClassResources);

		applyAdHocAspectsToModel(model);

		validateModel(model);

		registerAspectsFromModel(model);

		return loadBeanDefinitionsFromModel(model);
	}

	public int loadBeanDefinitions(Resource configClassResource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new Resource[] { configClassResource } );
	}

	protected abstract ConfigurationModel createConfigurationModel(Resource... configClassResources);

	protected abstract void applyAdHocAspectsToModel(ConfigurationModel model);

	protected void validateModel(ConfigurationModel model) {
		model.assertIsValid();
	}

	protected abstract void registerAspectsFromModel(ConfigurationModel model);

	/**
	 * @param model
	 * @return number of bean definitions registered
	 */
	protected int loadBeanDefinitionsFromModel(ConfigurationModel model) {
		return modelBeanDefinitionReader.loadBeanDefinitions(model);
	}

	protected ClassPathResource[] getAspectClassResources() {
		return aspectClassResources.toArray(new ClassPathResource[aspectClassResources.size()]);
	}

	// TODO: document this extensively.  the declaring class logic is quite complex, potentially confusing right now.
	private void initializeDeclaringClassBeanFactoryFactory() {
		beanFactory.registerBeanDefinition(BeanFactoryFactory.BEAN_NAME, BeanFactoryFactory.createBeanDefinition());
	}

}