package org.springframework.webflow.engine.impl;

import junit.framework.TestCase;

import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SimpleFlow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionImplFactoryTests extends TestCase {
	private FlowExecutionImplFactory factory = new FlowExecutionImplFactory();

	private Flow flowDefinition = new SimpleFlow();

	private boolean starting;
	
	public void testDefaultFactory() {
		FlowExecution execution = factory.createFlowExecution(flowDefinition);
		assertFalse(execution.isActive());
	}
	
	public void testFactoryWithExecutionAttributes() {
		MutableAttributeMap attributes = new LocalAttributeMap();
		attributes.put("foo", "bar");
		factory.setExecutionAttributes(attributes);
		FlowExecution execution = factory.createFlowExecution(flowDefinition);
		assertFalse(execution.isActive());
		assertEquals(attributes, execution.getAttributes());
	}
	
	public void testFactoryWithListener() {
		FlowExecutionListener listener1 = new FlowExecutionListenerAdapter() {
			public void sessionStarting(RequestContext context, FlowDefinition definition, MutableAttributeMap input) {
				starting = true;
			}
		};
		factory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(listener1));
		FlowExecution execution = factory.createFlowExecution(flowDefinition);
		assertFalse(execution.isActive());
		execution.start(null, new MockExternalContext());
		assertTrue(starting);
	}
}