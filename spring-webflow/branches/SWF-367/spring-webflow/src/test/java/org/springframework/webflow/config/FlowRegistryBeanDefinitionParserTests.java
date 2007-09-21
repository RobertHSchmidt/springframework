package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.webflow.definition.FlowDefinition;
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
	}

	public void testBogusPathWithValidExtension() {
		try {
			FlowDefinition flow = registry.getFlowDefinition("bogus");
		} catch (NoSuchFlowDefinitionException e) {

		}
	}
}
