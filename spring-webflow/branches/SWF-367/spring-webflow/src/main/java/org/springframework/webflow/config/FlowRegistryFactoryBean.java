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
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;

class FlowRegistryFactoryBean implements FactoryBean, ResourceLoaderAware, BeanFactoryAware, InitializingBean {

	private FlowDefinitionRegistry registry;

	private FlowLocation[] flowLocations;

	private FlowBuilderServices builderServices;

	private FlowDefinitionResourceFactory flowResourceFactory;

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
		flowResourceFactory = new FlowDefinitionResourceFactory(resourceLoader);
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
		FlowDefinitionResource flowResource = createResource(location);
		FlowBuilder builder = createFlowBuilder(flowResource);
		FlowAssembler assembler = new FlowAssembler(flowResource.getId(), builder, flowResource.getAttributes());
		return new RefreshableFlowDefinitionHolder(assembler);
	}

	private FlowDefinitionResource createResource(FlowLocation location) {
		MutableAttributeMap flowAttributes = null;
		if (!location.getAttributes().isEmpty()) {
			flowAttributes = new LocalAttributeMap();
			for (Iterator it = location.getAttributes().iterator(); it.hasNext();) {
				FlowElementAttribute attribute = (FlowElementAttribute) it.next();
				flowAttributes.put(attribute.getName(), getConvertedValue(attribute));
			}
		}
		return flowResourceFactory.createResource(location.getPath(), flowAttributes, location.getId());
	}

	private FlowBuilder createFlowBuilder(FlowDefinitionResource resource) {
		if (isXml(resource.getPath())) {
			return new XmlFlowBuilder(resource.getPath(), new DefaultFlowServiceLocator(registry, builderServices));
		} else {
			throw new IllegalArgumentException(resource
					+ " is not a supported resource type; supported types are [.xml]");
		}
	}

	private boolean isXml(Resource flowResource) {
		return flowResource.getFilename().endsWith(".xml");
	}

	private Object getConvertedValue(FlowElementAttribute attribute) {
		if (attribute.needsTypeConversion()) {
			ConversionExecutor converter = builderServices.getConversionService().getConversionExecutorByTargetAlias(
					String.class, attribute.getType());
			return converter.execute(attribute.getValue());
		} else {
			return attribute.getValue();
		}
	}

	private void initBuilderServices() {
		builderServices = new FlowBuilderServices();
		builderServices.setResourceLoader(resourceLoader);
		builderServices.setBeanFactory(beanFactory);
	}
}