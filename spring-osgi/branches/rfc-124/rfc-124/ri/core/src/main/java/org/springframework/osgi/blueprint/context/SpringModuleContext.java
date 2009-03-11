/*
 * Copyright 2008 the original author or authors.
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
 *
 */

package org.springframework.osgi.blueprint.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.context.ModuleContext;
import org.osgi.service.blueprint.context.NoSuchComponentException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.LocalComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceExportComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceComponentMetadata;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.blueprint.reflect.MetadataFactory;
import org.springframework.util.CollectionUtils;

/**
 * Default {@link ModuleContext} implementation. Wraps a Spring's
 * {@link ConfigurableListableBeanFactory} to the Blueprint Service's
 * ModuleContext interface. Additionally, this class adds RFC 124 specific
 * behaviour.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */

//TODO: use ConfigurableListableBeanFactory#isFrozen to cache the configuration
public class SpringModuleContext implements ModuleContext {

	/**
	 * cannot use a ConfigurableBeanFactory since the context is not yet
	 * refreshed at construction time
	 */
	private final ConfigurableApplicationContext applicationContext;
	private final BundleContext bundleContext;
	private transient ConfigurableListableBeanFactory beanFactory;


	public SpringModuleContext(ConfigurableApplicationContext applicationContext, BundleContext bundleContext) {
		this.applicationContext = applicationContext;
		this.bundleContext = bundleContext;
	}

	public Object getComponent(String name) throws NoSuchComponentException {
		if (applicationContext.containsBean(name)) {
			return applicationContext.getBean(name);
		}
		else {
			throw new NoSuchComponentException(name);
		}
	}

	public ComponentMetadata getComponentMetadata(String name) throws NoSuchComponentException {
		if (applicationContext.containsBeanDefinition(name)) {
			BeanDefinition beanDefinition = getBeanFactory().getBeanDefinition(name);
			return MetadataFactory.buildComponentMetadataFor(name, beanDefinition);
		}
		else {
			throw new NoSuchComponentException(name);
		}
	}

	public Set<String> getComponentNames() {
		String[] names = applicationContext.getBeanDefinitionNames();
		Set<String> components = new LinkedHashSet<String>(names.length);
		CollectionUtils.mergeArrayIntoCollection(names, components);
		return Collections.unmodifiableSet(components);
	}

	public Collection<ServiceExportComponentMetadata> getExportedServicesMetadata() {
		List<ComponentMetadata> metadatas = getComponentMetadataForAllComponents();
		List<ServiceExportComponentMetadata> serviceMetadata = new ArrayList<ServiceExportComponentMetadata>(
			metadatas.size());
		for (ComponentMetadata metadata : metadatas) {
			if (metadata instanceof ServiceExportComponentMetadata) {
				serviceMetadata.add((ServiceExportComponentMetadata) metadata);
			}
		}
		return Collections.unmodifiableCollection(serviceMetadata);
	}

	public Collection<ServiceReferenceComponentMetadata> getReferencedServicesMetadata() {
		List<ComponentMetadata> metadatas = getComponentMetadataForAllComponents();
		List<ServiceReferenceComponentMetadata> references = new ArrayList<ServiceReferenceComponentMetadata>(
			metadatas.size());
		for (ComponentMetadata metadata : metadatas) {
			if (metadata instanceof ServiceReferenceComponentMetadata) {
				references.add((ServiceReferenceComponentMetadata) metadata);
			}
		}
		return Collections.unmodifiableCollection(references);
	}

	public Collection<LocalComponentMetadata> getLocalComponentsMetadata() {
		List<ComponentMetadata> metadatas = getComponentMetadataForAllComponents();
		List<LocalComponentMetadata> references = new ArrayList<LocalComponentMetadata>(metadatas.size());
		for (ComponentMetadata metadata : metadatas) {
			if (metadata instanceof LocalComponentMetadata) {
				references.add((LocalComponentMetadata) metadata);
			}
		}
		return Collections.unmodifiableCollection(references);
	}

	public BundleContext getBundleContext() {
		return this.bundleContext;
	}

	private List<ComponentMetadata> getComponentMetadataForAllComponents() {
		List<ComponentMetadata> metadata = new ArrayList<ComponentMetadata>();
		String[] components = applicationContext.getBeanDefinitionNames();
		for (String beanName : components) {
			metadata.add(MetadataFactory.buildComponentMetadataFor(beanName, getBeanFactory().getBeanDefinition(
				beanName)));
		}
		return metadata;
	}

	private ConfigurableListableBeanFactory getBeanFactory() {
		if (beanFactory == null) {
			beanFactory = applicationContext.getBeanFactory();
		}

		return beanFactory;
	}
}