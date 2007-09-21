package org.springframework.webflow.config;

import java.util.HashSet;

import junit.framework.TestCase;

import org.springframework.webflow.config.FlowLocation.Attribute;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.builder.support.FlowBuilderSystemDefaults;

public class FlowRegistryFactoryBeanTests extends TestCase {
	private FlowRegistryFactoryBean factoryBean;

	public void setUp() {
		factoryBean = new FlowRegistryFactoryBean();
	}

	public void testGetFlowRegistry() throws Exception {
		HashSet attributes = new HashSet();
		attributes.add(new Attribute("foo", "bar", null));
		attributes.add(new Attribute("bar", "2", "integer"));
		FlowLocation location1 = new FlowLocation("flow1", "org/springframework/webflow/config/flow.xml", attributes);
		FlowLocation location2 = new FlowLocation("flow2", "org/springframework/webflow/config/flow.xml", attributes);
		FlowLocation[] flowLocations = new FlowLocation[] { location1, location2 };
		factoryBean.setFlowLocations(flowLocations);
		factoryBean.afterPropertiesSet();
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) factoryBean.getObject();
		FlowDefinition def = registry.getFlowDefinition("flow1");
		assertNotNull(def);
		assertEquals("flow1", def.getId());
		assertEquals("bar", def.getAttributes().get("foo"));
		assertEquals(new Integer(2), def.getAttributes().getInteger("bar"));
		def = registry.getFlowDefinition("flow2");
		assertNotNull(def);
		assertEquals("flow2", def.getId());
	}

	public void testGetFlowRegistryGeneratedFlowId() throws Exception {
		FlowLocation location1 = new FlowLocation(null, "org/springframework/webflow/config/flow.xml", null);
		FlowLocation[] flowLocations = new FlowLocation[] { location1 };
		factoryBean.setFlowLocations(flowLocations);
		factoryBean.afterPropertiesSet();
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) factoryBean.getObject();
		FlowDefinition def = registry.getFlowDefinition("flow");
		assertNotNull(def);
		assertEquals("flow", def.getId());
		assertTrue(def.getAttributes().isEmpty());
	}

	public void testGetFlowRegistryCustomFlowServices() throws Exception {
		FlowLocation location1 = new FlowLocation(null, "org/springframework/webflow/config/flow.xml", null);
		FlowLocation[] flowLocations = new FlowLocation[] { location1 };
		factoryBean.setFlowLocations(flowLocations);
		FlowBuilderServices builderServices = new FlowBuilderSystemDefaults().createBuilderServices();
		factoryBean.setFlowBuilderServices(builderServices);
		factoryBean.afterPropertiesSet();
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry) factoryBean.getObject();
		FlowDefinition def = registry.getFlowDefinition("flow");
		assertNotNull(def);
		assertEquals("flow", def.getId());
		assertTrue(def.getAttributes().isEmpty());
	}
}
