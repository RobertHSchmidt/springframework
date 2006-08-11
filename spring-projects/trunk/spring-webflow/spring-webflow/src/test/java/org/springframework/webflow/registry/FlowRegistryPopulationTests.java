package org.springframework.webflow.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.webflow.builder.SimpleFlowBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.AbstractFlowBuilder;
import org.springframework.webflow.engine.builder.BaseFlowServiceLocator;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.engine.builder.FlowServiceLocator;
import org.springframework.webflow.engine.builder.registry.DefaultFlowServiceLocator;
import org.springframework.webflow.engine.builder.registry.FlowRegistrarSupport;
import org.springframework.webflow.engine.builder.registry.FlowRegistryFactoryBean;
import org.springframework.webflow.engine.builder.registry.XmlFlowRegistrar;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowDefinitionRegistryImpl registry = new FlowDefinitionRegistryImpl();
		FlowServiceLocator factory = new BaseFlowServiceLocator();
		FlowBuilder builder1 = new AbstractFlowBuilder(factory) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		Flow flow1 = new FlowAssembler("flow1", builder1).assembleFlow();

		FlowBuilder builder2 = new AbstractFlowBuilder(factory) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		Flow flow2 = new FlowAssembler("flow2", builder2).assembleFlow();

		registry.registerFlowDefinition(new StaticFlowDefinitionHolder(flow1));
		registry.registerFlowDefinition(new StaticFlowDefinitionHolder(flow2));
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
	}

	public void testXmlPopulationWithRecursion() {
		FlowDefinitionRegistryImpl registry = new FlowDefinitionRegistryImpl();
		FlowServiceLocator flowArtifactFactory = new DefaultFlowServiceLocator(registry,
				new DefaultListableBeanFactory());
		File parent = new File("src/test/java/org/springframework/webflow/registry");
		XmlFlowRegistrar registrar = new XmlFlowRegistrar();
		registrar.addFlowLocation(new FileSystemResource(new File(parent, "flow1.xml")));
		registrar.addFlowLocation(new FileSystemResource(new File(parent, "flow2.xml")));
		registrar.addFlowDefinition(new FlowDefinitionResource("flow3", new FileSystemResource(new File(parent,
				"flow2.xml"))));
		registrar.registerFlows(registry, flowArtifactFactory);
		assertEquals("Wrong registry definition count", 3, registry.getFlowDefinitionCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 3, registry.getFlowDefinitionCount());
	}

	public void testFlowRegistryFactoryBean() throws Exception {
		GenericApplicationContext beanFactory = new GenericApplicationContext();
		FlowRegistryFactoryBean factoryBean = new FlowRegistryFactoryBean();
		factoryBean.setFlowRegistrar(new MyFlowRegistrar());
		factoryBean.setBeanFactory(beanFactory);
		factoryBean.afterPropertiesSet();
		FlowDefinitionRegistry registry = factoryBean.populateFlowRegistry();
		assertEquals("Wrong registry definition count", 3, registry.getFlowDefinitionCount());
	}

	public void testXmlFlowRegistryFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)ac.getBean("flowRegistry2");
		assertEquals("Wrong registry definition count", 7, registry.getFlowDefinitionCount());
	}

	public void testXmlFlowRegistryFactoryBeanFlowDefinitionProperties() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)ac.getBean("flowRegistry3");
		assertEquals("Wrong registry definition count", 2, registry.getFlowDefinitionCount());
		registry.getFlowDefinition("flow1");
		registry.getFlowDefinition("flow2");
	}

	public void testCustomFlowRegistryServicesWithParentRegistry() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)ac.getBean("flowRegistry4");
		assertEquals("Wrong registry definition count", 7, registry.getFlowDefinitionCount());
		registry.getFlowDefinition("flow1");
		registry.getFlowDefinition("flow2");
		registry.getFlowDefinition("flow5");
		registry.getFlowDefinition("flow6");
	}
	
	public static class MyFlowRegistrar extends FlowRegistrarSupport {
		public void registerFlows(FlowDefinitionRegistry registry, FlowServiceLocator flowArtifactFactory) {
			File parent = new File("src/test/org/springframework/webflow/registry");
			registerXmlFlow(new FileSystemResource(new File(parent, "flow1.xml")), registry, flowArtifactFactory);
			registerXmlFlow(new FileSystemResource(new File(parent, "flow2.xml")), registry, flowArtifactFactory);
			registerFlow("flow3", registry, new SimpleFlowBuilder());
		}
	}
}