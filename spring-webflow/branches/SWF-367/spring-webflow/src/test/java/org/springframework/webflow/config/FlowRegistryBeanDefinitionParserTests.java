package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;

public class FlowRegistryBeanDefinitionParserTests extends TestCase {
	private ClassPathXmlApplicationContext context;
	private FlowDefinitionRegistry registry;

	public void setUp() {
		context = new ClassPathXmlApplicationContext("org/springframework/webflow/config/flow-registry.xml");
		registry = (FlowDefinitionRegistry) context.getBean("flowRegistry");
	}

	public void testRegistryPopulated() {
		FlowDefinition flow = registry.getFlowDefinition("flow");
		assertEquals("flow", flow.getId());
		assertEquals("bar", flow.getAttributes().get("foo"));
		assertEquals(new Integer(2), flow.getAttributes().get("bar"));
	}

	public void testNoSuchFlow() {
		try {
			registry.getFlowDefinition("not there");
		} catch (NoSuchFlowDefinitionException e) {

		}
	}

	public void testBogusPath() {
		try {
			registry.getFlowDefinition("bogus");
		} catch (FlowDefinitionConstructionException e) {

		}
	}
}
