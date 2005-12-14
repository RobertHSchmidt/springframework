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
		FlowArtifactFactory factory = new FlowArtifactFactoryAdapter();
		FlowBuilder builder1 = new AbstractFlowBuilder(factory) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		new FlowAssembler("flow1", builder1).assembleFlow();

		FlowBuilder builder2 = new AbstractFlowBuilder(factory) {
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
		FlowArtifactFactory flowArtifactFactory = new FlowRegistryFlowArtifactFactory(registry,
				new DefaultListableBeanFactory());
		File parent = new File("src/test/org/springframework/webflow/registry");
		Resource[] locations = new Resource[] { new FileSystemResource(new File(parent, "flow1.xml")),
				new FileSystemResource(new File(parent, "flow2.xml")) };
		new XmlFlowRegistrar(locations).registerFlows(registry, flowArtifactFactory);
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
	}

	public void testFlowRegistryFactoryBean() throws Exception {
		GenericApplicationContext beanFactory = new GenericApplicationContext();
		FlowRegistryFactoryBean factoryBean = new FlowRegistryFactoryBean();
		factoryBean.setFlowRegistrar(new MyFlowRegistrar());
		factoryBean.setBeanFactory(beanFactory);
		FlowRegistry registry = (FlowRegistry)factoryBean.getObject();
		assertEquals("Wrong registry definition count", 3, registry.getFlowCount());
	}

	public void testXmlFlowRegistryFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry");
		assertEquals("Wrong registry definition count", 4, registry.getFlowCount());
	}

	public static class MyFlowRegistrar extends FlowRegistrarSupport {
		public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
			File parent = new File("src/test/org/springframework/webflow/registry");
			registerXmlFlow("flow1", new File(parent, "flow1.xml"), flowArtifactFactory, registry);
			registerXmlFlow("flow2", new File(parent, "flow2.xml"), flowArtifactFactory, registry);
			registerFlow("flow3", new SimpleFlowBuilder(), registry);
		}
	}
	
	public static class SimpleFlowBuilder extends AbstractFlowBuilder {
		public void buildStates() {
			addEndState("end");
		}
	}

}