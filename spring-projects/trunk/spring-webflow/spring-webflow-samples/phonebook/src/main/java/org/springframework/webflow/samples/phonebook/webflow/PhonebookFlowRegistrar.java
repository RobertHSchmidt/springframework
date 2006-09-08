package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowServiceLocator;

/**
 * Demonstrates how to register flows programatically.
 * 
 * @author Keith Donald
 */
class PhonebookFlowRegistrar implements FlowDefinitionRegistrar {
	private FlowServiceLocator serviceLocator;

	public PhonebookFlowRegistrar(FlowServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	public void registerFlowDefinitions(FlowDefinitionRegistry registry) {
		registry.registerFlowDefinition(assemble("detail-flow", new PersonDetailFlowBuilder(serviceLocator)));
		registry.registerFlowDefinition(assemble("search-flow", new SearchPersonFlowBuilder(serviceLocator)));
	}

	private FlowDefinitionHolder assemble(String flowId, FlowBuilder flowBuilder) {
		return new StaticFlowDefinitionHolder(new FlowAssembler(flowId, flowBuilder).assembleFlow());
	}
}