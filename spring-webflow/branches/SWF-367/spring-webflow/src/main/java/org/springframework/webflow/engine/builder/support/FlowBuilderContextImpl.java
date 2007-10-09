package org.springframework.webflow.engine.builder.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.builder.FlowBuilderContext;

public class FlowBuilderContextImpl implements FlowBuilderContext {

	private String flowId;

	private AttributeMap flowAttributes;

	private FlowDefinitionLocator flowDefinitionLocator;

	private FlowBuilderServices flowBuilderServices;

	public FlowBuilderContextImpl(String flowId, AttributeMap flowAttributes,
			FlowDefinitionLocator flowDefinitionLocator, FlowBuilderServices flowBuilderServices) {
		this.flowId = flowId;
		this.flowAttributes = flowAttributes;
		this.flowDefinitionLocator = flowDefinitionLocator;
		this.flowBuilderServices = flowBuilderServices;
	}

	public String getFlowId() {
		return flowId;
	}

	public AttributeMap getFlowAttributes() {
		return flowAttributes;
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowBuilderServices.getFlowArtifactFactory();
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return flowBuilderServices.getBeanInvokingActionFactory();
	}

	public ViewFactoryCreator getViewFactoryCreator() {
		return flowBuilderServices.getViewFactoryCreator();
	}

	public ExpressionParser getExpressionParser() {
		return flowBuilderServices.getExpressionParser();
	}

	public ConversionService getConversionService() {
		return flowBuilderServices.getConversionService();
	}

	public ResourceLoader getResourceLoader() {
		return flowBuilderServices.getResourceLoader();
	}

	public BeanFactory getBeanFactory() {
		return flowBuilderServices.getBeanFactory();
	}

	public FlowDefinitionLocator getFlowDefinitionLocator() {
		return flowDefinitionLocator;
	}
}
