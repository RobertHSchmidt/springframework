package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.test.MockFlowServiceLocator;

public class XmlFlowBuilderTests extends TestCase {
	private XmlFlowBuilder builder;
	private MockFlowServiceLocator serviceLocator;

	protected void setUp() {
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("bean", new Object());
		serviceLocator = new MockFlowServiceLocator();
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
