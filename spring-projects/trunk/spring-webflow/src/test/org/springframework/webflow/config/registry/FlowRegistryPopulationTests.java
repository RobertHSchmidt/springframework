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
import org.springframework.webflow.config.FlowArtifactFactory;
import org.springframework.webflow.config.FlowArtifactFactoryAdapter;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.FlowBuilderException;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowArtifactFactory artifactLocator = new FlowArtifactFactoryAdapter();
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
	
	public void testXmlPopulationWithRecursion() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		File parent = new File("src/test/org/springframework/webflow/config/registry");
		Resource[] locations = new Resource[] {
				new FileSystemResource(new File(parent, "flow1.xml")),
				new FileSystemResource(new File(parent, "flow2.xml"))
		};
		new XmlFlowRegistrar(new FlowArtifactFactoryAdapter(registry), locations).registerDefinitions(registry);
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
}