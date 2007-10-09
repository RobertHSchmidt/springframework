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
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.builder.RefreshableFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.support.FlowBuilderContextImpl;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;

class FlowRegistryFactoryBean implements FactoryBean, ResourceLoaderAware, BeanFactoryAware, InitializingBean {

	private FlowDefinitionRegistry flowRegistry;

	private FlowLocation[] flowLocations;

	private FlowBuilderServices flowBuilderServices;

	private FlowDefinitionResourceFactory flowResourceFactory;

	private ResourceLoader resourceLoader;

	private BeanFactory beanFactory;

	public void setFlowLocations(FlowLocation[] flowLocations) {
		this.flowLocations = flowLocations;
	}

	public void setFlowBuilderServices(FlowBuilderServices flowBuilderServices) {
		this.flowBuilderServices = flowBuilderServices;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if (flowBuilderServices == null) {
			initFlowBuilderServices();
		}
		flowResourceFactory = new FlowDefinitionResourceFactory(resourceLoader);
		flowRegistry = new FlowDefinitionRegistryImpl();
		for (int i = 0; i < flowLocations.length; i++) {
			FlowLocation location = flowLocations[i];
			flowRegistry.registerFlowDefinition(createFlowDefinitionHolder(location));
		}
	}

	public Object getObject() throws Exception {
		return flowRegistry;
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
		FlowBuilderContext builderContext = new FlowBuilderContextImpl(flowResource.getId(), flowResource
				.getAttributes(), flowRegistry, flowBuilderServices);
		FlowAssembler assembler = new FlowAssembler(builder, builderContext);
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
			return new XmlFlowBuilder(resource.getPath());
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
			ConversionExecutor converter = flowBuilderServices.getConversionService()
					.getConversionExecutorByTargetAlias(String.class, attribute.getType());
			return converter.execute(attribute.getValue());
		} else {
			return attribute.getValue();
		}
	}

	private void initFlowBuilderServices() {
		flowBuilderServices = new FlowBuilderServices();
		flowBuilderServices.setResourceLoader(resourceLoader);
		flowBuilderServices.setBeanFactory(beanFactory);
	}
}