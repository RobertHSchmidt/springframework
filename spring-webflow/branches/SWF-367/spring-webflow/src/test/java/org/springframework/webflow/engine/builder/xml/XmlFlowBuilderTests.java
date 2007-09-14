package org.springframework.webflow.engine.builder.xml;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.engine.builder.support.DefaultFlowServiceLocator;

import junit.framework.TestCase;

public class XmlFlowBuilderTests extends TestCase {
	private XmlFlowBuilder builder;
	private DefaultFlowServiceLocator serviceLocator;

	protected void setUp() {
		FlowDefinitionLocator subflowLocator = new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return new Flow("subflow");
			}
		};
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("bean", new Object());
		serviceLocator = new DefaultFlowServiceLocator(subflowLocator, beanFactory);
	}

	public void testBuildIncompleteFlow() {
		ClassPathResource resource = new ClassPathResource("flow-incomplete.xml", getClass());
		builder = new XmlFlowBuilder(resource, serviceLocator);
		FlowAssembler assembler = new FlowAssembler("flow", builder);
		try {
			assembler.assembleFlow();
			fail("Should have failed");
		} catch (FlowBuilderException e) {

		}
	}

	public void testBuildFlowWithEndState() {
		ClassPathResource resource = new ClassPathResource("flow-endstate.xml", getClass());
		builder = new XmlFlowBuilder(resource, serviceLocator);
		FlowAssembler assembler = new FlowAssembler("flow", builder);
		Flow flow = assembler.assembleFlow();
		assertEquals("flow", flow.getId());
		assertEquals("end", flow.getStartState().getId());
	}
}
