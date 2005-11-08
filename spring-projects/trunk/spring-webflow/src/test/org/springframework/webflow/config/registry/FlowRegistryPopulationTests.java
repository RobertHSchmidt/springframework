package org.springframework.webflow.config.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.AbstractFlowBuilder;
import org.springframework.webflow.config.FlowArtifactLocator;
import org.springframework.webflow.config.FlowArtifactLocatorAdapter;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.FlowBuilderException;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowArtifactLocator artifactLocator = new FlowArtifactLocatorAdapter();
		FlowBuilder[] builders = new FlowBuilder[] {
			new AbstractFlowBuilder(artifactLocator) {
				protected String flowId() {
					return "flow1";
				}
				public void buildStates() throws FlowBuilderException {
					addEndState("end");
				}
			},
			new AbstractFlowBuilder(artifactLocator) {
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
		XmlFlowRegistrar registrar = new XmlFlowRegistrar(new FlowArtifactLocatorAdapter());
		registrar.setDefinitionLocations(locations);
		registrar.registerFlowDefinitions(registry);
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}
	
	public void testXmlFlowRegistryFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry");
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}

	public void testXmlFlowFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		Flow flow1 = (Flow)ac.getBean("flow1");
		assertEquals("Wrong flow id", "flow1", flow1.getId());
	}

}
