package org.springframework.webflow.config.registry;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.config.AbstractFlowBuilder;
import org.springframework.webflow.config.FlowArtifactLocatorAdapter;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.FlowBuilderException;
import org.springframework.webflow.config.registry.FlowRegistrarImpl;
import org.springframework.webflow.config.registry.FlowRegistryImpl;
import org.springframework.webflow.config.registry.XmlFlowRegistrar;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowBuilder[] builders = new FlowBuilder[] {
				new AbstractFlowBuilder() {
					protected String flowId() {
						return "flow1";
					}
					public void buildStates() throws FlowBuilderException {
						addEndState("end");
					}
				},
				new AbstractFlowBuilder() {
					protected String flowId() {
						return "flow2";
					}
					public void buildStates() throws FlowBuilderException {
						addEndState("end");
					}
				}
		};
		FlowRegistrarImpl registrar = new FlowRegistrarImpl(builders);
		registrar.registerFlowDefinitions(registry);
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}
	
	public void testXmlPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		Resource[] locations = new Resource[] {
				new FileSystemResource("src/test/org/springframework/webflow/config/registry/flow1.xml"),
				new FileSystemResource("src/test/org/springframework/webflow/config/registry/flow2.xml")
		};
		XmlFlowRegistrar registrar = new XmlFlowRegistrar(new FlowArtifactLocatorAdapter(), locations);
		registrar.registerFlowDefinitions(registry);
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}
}
