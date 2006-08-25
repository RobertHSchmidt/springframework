package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.test.MockExternalContext;

public class EvaluateActionXmlFlowBuilderTests extends TestCase {
	private Flow flow;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("evaluateActionFlow.xml",
				XmlFlowBuilderTests.class), new TestFlowServiceLocator());
		new FlowAssembler("evaluateActionFlow", builder).assembleFlow();
		flow = builder.getFlow();
	}

	public void testActionStateConfiguration() {
		ActionState as1 = (ActionState)flow.getState("actionState1");
	}

	public void testFlowExecution() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		FlowExecution execution = factory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
	}
}