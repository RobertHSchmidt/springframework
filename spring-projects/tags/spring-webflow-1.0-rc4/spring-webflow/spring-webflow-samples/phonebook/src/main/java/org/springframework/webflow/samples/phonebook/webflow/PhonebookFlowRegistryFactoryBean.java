package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.AbstractFlowBuildingFlowRegistryFactoryBean;

/**
 * Demonstrates how to populate a flow registry programatically.
 * 
 * @author Keith Donald
 */
public class PhonebookFlowRegistryFactoryBean extends AbstractFlowBuildingFlowRegistryFactoryBean {
	protected void doPopulate(FlowDefinitionRegistry registry) {
		new PhonebookFlowRegistrar(getFlowServiceLocator()).registerFlowDefinitions(registry);
	}
}
