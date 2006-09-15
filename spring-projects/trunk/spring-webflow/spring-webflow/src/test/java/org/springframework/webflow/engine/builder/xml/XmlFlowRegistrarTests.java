package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.engine.builder.BaseFlowServiceLocator;

public class XmlFlowRegistrarTests extends TestCase {
	private XmlFlowRegistrar registrar;

	private FlowDefinitionRegistry registry = new FlowDefinitionRegistryImpl();

	protected void setUp() {
		BaseFlowServiceLocator locator = new BaseFlowServiceLocator();
		registrar = new XmlFlowRegistrar(locator);
	}

	public void testAddLocation() {
		assertEquals(0, registry.getFlowDefinitionCount());
		registrar.addLocation(new ClassPathResource("flow.xml", getClass()));
		registrar.registerFlowDefinitions(registry);
		assertEquals(1, registry.getFlowDefinitionCount());
		assertEquals("flow", registry.getFlowDefinitions()[0].getId());
	}

	public void testAddResource() {
		assertEquals(0, registry.getFlowDefinitionCount());
		registrar.addResource(new FlowDefinitionResource("foo", new ClassPathResource("flow.xml", getClass())));
		registrar.registerFlowDefinitions(registry);
		assertEquals(1, registry.getFlowDefinitionCount());
		assertEquals("foo", registry.getFlowDefinitions()[0].getId());
	}
}