package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.test.MockFlowServiceLocator;

public class XmlFlowRegistrarTests extends TestCase {
	private MockFlowServiceLocator serviceLocator;
	private XmlFlowRegistrar registrar;

	protected void setUp() {
		serviceLocator = new MockFlowServiceLocator();
		registrar = new XmlFlowRegistrar(serviceLocator);
	}

	public void testRegisterFlow() {
		registrar.addResource(FlowDefinitionResource.create("flow-registrar.xml"));
		FlowDefinitionRegistry registry = serviceLocator.getMockSubflowRegistry();
		registrar.registerFlowDefinitions(registry);
		assertEquals(1, registry.getFlowDefinitionCount());
	}
}
