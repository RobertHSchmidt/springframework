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
package org.springframework.config.java.model;

import java.util.List;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.core.BeanFactoryFactory;
import org.springframework.config.java.core.Constants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class AbstractJavaConfigBeanDefinitionReader extends AbstractBeanDefinitionReader implements JavaConfigBeanDefinitionReader {
	private final List<ClassPathResource> aspectClassResources;
	private final ConfigurationModelAspectRegistry aspectRegistry;
	private final ConfigurationModelBeanDefinitionReader modelBeanDefinitionReader;

	@Deprecated
	protected AbstractJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, null);
	}

	protected AbstractJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry, List<ClassPathResource> aspectClassResources) {
		super(registry);
		this.aspectClassResources = aspectClassResources;

		initializeDeclaringClassBeanFactoryFactory();

		aspectRegistry = initializeAspectRegistry();

		modelBeanDefinitionReader = initializeConfigurationModelBeanDefinitionReader();
	}

	@Override
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

	protected void registerAspectsFromModel(ConfigurationModel model) {
		aspectRegistry.registerAspects(model, (BeanFactory) getRegistry());
	}

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
		RootBeanDefinition bff = new RootBeanDefinition();
		String factoryName = BeanFactoryFactory.class.getName();
		bff.setBeanClassName(factoryName);
		bff.setFactoryMethodName(BeanFactoryFactory.FACTORY_METHOD_NAME);
		bff.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		bff.addMetadataAttribute(new BeanMetadataAttribute(Constants.JAVA_CONFIG_IGNORE, true));

		this.getRegistry().registerBeanDefinition(factoryName, bff);
	}

	private ConfigurationModelBeanDefinitionReader initializeConfigurationModelBeanDefinitionReader() {
		return new ConfigurationModelBeanDefinitionReader(this.getRegistry());
	}

	private ConfigurationModelAspectRegistry initializeAspectRegistry() {
		ConfigurationModelAspectRegistry aspectRegistry = new ConfigurationModelAspectRegistry();
		String aspectRegistryBeanName = ConfigurationModelAspectRegistry.class.getName();
		if(!((SingletonBeanRegistry)getRegistry()).containsSingleton(aspectRegistryBeanName))
			((SingletonBeanRegistry)getRegistry()).registerSingleton(aspectRegistryBeanName, aspectRegistry);
		return aspectRegistry;
	}

}