package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

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
import org.springframework.webflow.engine.builder.support.FlowBuilderSystemDefaults;

public class XmlFlowBuilderTests extends TestCase {
	private XmlFlowBuilder builder;
	private DefaultFlowServiceLocator serviceLocator;

	protected void setUp() {
		FlowDefinitionLocator subflowLocator = new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return new Flow(flowId);
			}
		};
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("bean", new Object());
		serviceLocator = new DefaultFlowServiceLocator(subflowLocator, FlowBuilderSystemDefaults.get());
	}

	public void testBuildIncompleteFlow() {
		ClassPathResource resource = new ClassPathResource("flow-incomplete.xml", getClass());
		builder = new XmlFlowBuilder(resource, serviceLocator);
		FlowAssembler assembler = new FlowAssembler("flow", builder, null);
		try {
			assembler.assembleFlow();
			fail("Should have failed");
		} catch (FlowBuilderException e) {

		}
	}

	public void testBuildFlowWithEndState() {
		ClassPathResource resource = new ClassPathResource("flow-endstate.xml", getClass());
		builder = new XmlFlowBuilder(resource, serviceLocator);
		FlowAssembler assembler = new FlowAssembler("flow", builder, null);
		Flow flow = assembler.assembleFlow();
		assertEquals("flow", flow.getId());
		assertEquals("end", flow.getStartState().getId());
	}
}
