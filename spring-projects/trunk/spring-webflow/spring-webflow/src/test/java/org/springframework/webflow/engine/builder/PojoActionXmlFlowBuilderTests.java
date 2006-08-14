package org.springframework.webflow.engine.builder;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.action.bean.AbstractBeanInvokingAction;
import org.springframework.webflow.action.bean.MethodResultSpecification;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;
import org.springframework.webflow.execution.ScopeType;

public class PojoActionXmlFlowBuilderTests extends TestCase {
	private Flow flow;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("pojoActionFlow.xml",
				XmlFlowBuilderTests.class), new TestFlowArtifactFactory());
		new FlowAssembler("pojoActionFlow", builder).assembleFlow();
		flow = builder.getFlow();
	}

	public void testActionStateConfiguration() {
		ActionState as1 = (ActionState)flow.getRequiredState("actionState1");
		AbstractBeanInvokingAction targetAction = (AbstractBeanInvokingAction)as1.getActionList().getAnnotated(0)
				.getTargetAction();
		assertEquals(ScopeType.REQUEST, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());

		ActionState as2 = (ActionState)flow.getRequiredState("actionState2");
		targetAction = (AbstractBeanInvokingAction)as2.getActionList().getAnnotated(0).getTargetAction();
		assertEquals(ScopeType.FLOW, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());

		ActionState as3 = (ActionState)flow.getRequiredState("actionState3");
		targetAction = (AbstractBeanInvokingAction)as3.getActionList().getAnnotated(0).getTargetAction();
		assertEquals(ScopeType.CONVERSATION, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());
	}
}