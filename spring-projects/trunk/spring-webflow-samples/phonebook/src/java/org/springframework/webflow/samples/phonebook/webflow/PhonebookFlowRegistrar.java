package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistrarSupport;
import org.springframework.webflow.registry.FlowRegistry;

/**
 * Demonstrates how to register flows programatically.
 * 
 * @author Keith Donald
 */
public class PhonebookFlowRegistrar extends FlowRegistrarSupport {
	public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		registerFlow("search", new SearchPersonFlowBuilder(flowArtifactFactory), registry);
		registerFlow("detail", new PersonDetailFlowBuilder(flowArtifactFactory), registry);
	}
}