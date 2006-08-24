package org.springframework.webflow.engine.builder.xml;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.action.bean.AbstractBeanInvokingAction;
import org.springframework.webflow.action.bean.MethodResultSpecification;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.MockExternalContext;

public class PojoActionXmlFlowBuilderTests extends TestCase {
	private Flow flow;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("pojoActionFlow.xml",
				XmlFlowBuilderTests.class), new TestFlowServiceLocator());
		new FlowAssembler("pojoActionFlow", builder).assembleFlow();
		flow = builder.getFlow();
	}

	public void testActionStateConfiguration() {
		ActionState as1 = (ActionState)flow.getState("actionState1");
		AbstractBeanInvokingAction targetAction = (AbstractBeanInvokingAction)as1.getActionList().getAnnotated(0)
				.getTargetAction();
		assertEquals(ScopeType.REQUEST, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());
		assertEquals(1, as1.getTransitionSet().size());

		ActionState as2 = (ActionState)flow.getState("actionState2");
		targetAction = (AbstractBeanInvokingAction)as2.getActionList().getAnnotated(0).getTargetAction();
		assertEquals(ScopeType.FLOW, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());

		ActionState as3 = (ActionState)flow.getState("actionState3");
		targetAction = (AbstractBeanInvokingAction)as3.getActionList().getAnnotated(0).getTargetAction();
		assertEquals(ScopeType.CONVERSATION, ((MethodResultSpecification)targetAction.getMethodResultSpecification()).getResultScope());

		ActionState as4 = (ActionState)flow.getState("actionState4");
		targetAction = (AbstractBeanInvokingAction)as4.getActionList().getAnnotated(0).getTargetAction();
		assertEquals("methodWithVariableArgument", targetAction.getMethodSignature().getMethodName());
		assertEquals(1, targetAction.getMethodSignature().getParameters().size());
		//assertEquals("flowScope.result2", targetAction.getMethodSignature().getParameters().getParameter(0).getName());
		assertEquals(null, targetAction.getMethodResultSpecification());

		ActionState as5 = (ActionState)flow.getState("actionState5");
		targetAction = (AbstractBeanInvokingAction)as5.getActionList().getAnnotated(0).getTargetAction();
		assertEquals("methodWithConstantArgument", targetAction.getMethodSignature().getMethodName());
		assertEquals(1, targetAction.getMethodSignature().getParameters().size());
		assertEquals(null, targetAction.getMethodResultSpecification());

		ActionState as6 = (ActionState)flow.getState("actionState6");
		targetAction = (AbstractBeanInvokingAction)as6.getActionList().getAnnotated(0).getTargetAction();
		assertEquals("methodWithArgumentTypeConversion", targetAction.getMethodSignature().getMethodName());
		assertEquals(1, targetAction.getMethodSignature().getParameters().size());
		assertEquals(null, targetAction.getMethodResultSpecification());
	}
	
	public void testFlowExecution() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		FlowExecution execution = factory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
	}
}