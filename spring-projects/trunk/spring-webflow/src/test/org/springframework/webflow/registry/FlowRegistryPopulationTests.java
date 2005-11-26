package org.springframework.webflow.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactFactoryAdapter;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.FlowBuilderException;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowArtifactFactory artifactLocator = new FlowArtifactFactoryAdapter();
		FlowBuilder builder1 = new AbstractFlowBuilder(artifactLocator) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		new FlowAssembler("flow1", builder1).assembleFlow();

		FlowBuilder builder2 = new AbstractFlowBuilder(artifactLocator) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		new FlowAssembler("flow2", builder2).assembleFlow();

		registry.registerFlow(new StaticFlowHolder(builder1.getResult()));
		registry.registerFlow(new StaticFlowHolder(builder2.getResult()));
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
	}

	public static class StaticFlowHolder implements FlowHolder {
		private Flow flow;

		public StaticFlowHolder(Flow flow) {
			this.flow = flow;
		}

		public Flow getFlow() {
			return flow;
		}

		public String getId() {
			return flow.getId();
		}

		public void refresh() {
		}
	}

	public void testXmlPopulationWithRecursion() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		File parent = new File("src/test/org/springframework/webflow/registry");
		Resource[] locations = new Resource[] { new FileSystemResource(new File(parent, "flow1.xml")),
				new FileSystemResource(new File(parent, "flow2.xml")) };
		new XmlFlowRegistrar(new FlowRegistryFlowArtifactFactory(registry, new DefaultListableBeanFactory()), locations)
				.registerDefinitions(registry);
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
	}

	public void testXmlFlowRegistryFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry");
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
	}
}