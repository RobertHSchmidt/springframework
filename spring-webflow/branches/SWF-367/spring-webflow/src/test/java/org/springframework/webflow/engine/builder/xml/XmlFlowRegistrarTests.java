package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.support.FlowDefinitionResource;
import org.springframework.webflow.test.MockFlowServiceLocator;

public class XmlFlowRegistrarTests extends TestCase {
	private MockFlowServiceLocator serviceLocator;
	private XmlFlowRegistrar registrar;

	protected void setUp() {
		serviceLocator = new MockFlowServiceLocator();
		registrar = new XmlFlowRegistrar(serviceLocator);
	}

	public void testRegisterFlow() {
		registrar.addResource(FlowDefinitionResource
				.create("org/springframework/webflow/engine/builder/xml/flow-registrar.xml"));
		FlowDefinitionRegistry registry = serviceLocator.getMockSubflowRegistry();
		registrar.registerFlowDefinitions(registry);
		FlowDefinition def = registry.getFlowDefinition("flow-registrar");
		assertEquals("flow-registrar", def.getId());
	}
}
