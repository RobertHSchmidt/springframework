package org.springframework.webflow.config;

import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.config.FlowLocation.Attribute;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.RefreshableFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.support.DefaultFlowServiceLocator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.builder.support.FlowBuilderSystemDefaults;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;

public class FlowRegistryFactoryBean implements FactoryBean, ResourceLoaderAware, BeanFactoryAware, InitializingBean {

	private FlowDefinitionRegistry registry;

	private FlowLocation[] flowLocations;

	private FlowBuilderServices builderServices;

	private ResourceLoader resourceLoader;

	private BeanFactory beanFactory;

	public void setFlowLocations(FlowLocation[] flowLocations) {
		this.flowLocations = flowLocations;
	}

	public void setFlowBuilderServices(FlowBuilderServices builderServices) {
		this.builderServices = builderServices;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if (builderServices == null) {
			initBuilderServices();
		}
		registry = new FlowDefinitionRegistryImpl();
		for (int i = 0; i < flowLocations.length; i++) {
			FlowLocation location = flowLocations[i];
			registry.registerFlowDefinition(createFlowDefinitionHolder(location));
		}
	}

	public Object getObject() throws Exception {
		return registry;
	}

	public Class getObjectType() {
		return FlowDefinitionRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private FlowDefinitionHolder createFlowDefinitionHolder(FlowLocation location) {
		String flowId = location.getId();
		Resource flowResource = builderServices.getResourceLoader().getResource(location.getPath());
		if (flowId == null) {
			flowId = getFlowId(flowResource);
		}
		MutableAttributeMap flowAttributes = new LocalAttributeMap();
		for (Iterator it = location.getAttributes().iterator(); it.hasNext();) {
			Attribute attribute = (Attribute) it.next();
			flowAttributes.put(attribute.getName(), getConvertedValue(attribute));
		}
		FlowBuilder flowBuilder = createFlowBuilder(flowResource);
		return new RefreshableFlowDefinitionHolder(new FlowAssembler(flowId, flowBuilder, flowAttributes));
	}

	private FlowBuilder createFlowBuilder(Resource flowResource) {
		if (isXml(flowResource)) {
			return new XmlFlowBuilder(flowResource, new DefaultFlowServiceLocator(registry, builderServices));
		} else {
			throw new IllegalArgumentException("Resource " + flowResource
					+ " is not a supported resource type; supported types are [.xml]");
		}
	}

	private boolean isXml(Resource flowResource) {
		return flowResource.getFilename().endsWith(".xml");
	}

	private Object getConvertedValue(Attribute attribute) {
		if (attribute.getType() != null) {
			ConversionExecutor converter = builderServices.getConversionService().getConversionExecutorByTargetAlias(
					String.class, attribute.getType());
			return converter.execute(attribute.getValue());
		} else {
			return attribute.getValue();
		}
	}

	private String getFlowId(Resource flowResource) {
		String fileName = flowResource.getFilename();
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex != -1) {
			return fileName.substring(0, extensionIndex);
		} else {
			return fileName;
		}
	}

	private void initBuilderServices() {
		builderServices = new FlowBuilderSystemDefaults().createBuilderServices();
		if (resourceLoader != null) {
			builderServices.setResourceLoader(resourceLoader);
		}
		if (beanFactory != null) {
			builderServices.setBeanFactory(beanFactory);
		}
	}
}