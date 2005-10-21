package org.springframework.webflow.config.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.config.AbstractFlowBuilder;
import org.springframework.webflow.config.FlowArtifactLocatorAdapter;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.FlowBuilderException;

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
		File parent = new File("src/test/org/springframework/webflow/config/registry");
		Resource[] locations = new Resource[] {
				new FileSystemResource(new File(parent, "flow1.xml")),
				new FileSystemResource(new File(parent, "flow2.xml"))
		};
		XmlFlowRegistrar registrar = new XmlFlowRegistrar(new FlowArtifactLocatorAdapter(), locations);
		registrar.registerFlowDefinitions(registry);
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}
}
