package org.springframework.webflow.samples.phonebook.web;

import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.registry.AbstractFlowRegistryFactoryBean;
import org.springframework.webflow.registry.FlowHolder;
import org.springframework.webflow.registry.FlowRegistry;
import org.springframework.webflow.registry.RefreshingFlowHolder;

/**
 * Demonstrates how to registry flows programatically.
 * 
 * @author Keith Donald
 */
public class FlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {
	protected void doPopulate(FlowRegistry registry) {
		registry.registerFlow(getSearchFlow());
		registry.registerFlow(getDetailFlow());
	}

	private FlowHolder getSearchFlow() {
		return new RefreshingFlowHolder(new FlowAssembler("detail", new PersonDetailFlowBuilder()));
	}

	private FlowHolder getDetailFlow() {
		return new RefreshingFlowHolder(new FlowAssembler("search", new SearchPersonFlowBuilder()));
	}
}