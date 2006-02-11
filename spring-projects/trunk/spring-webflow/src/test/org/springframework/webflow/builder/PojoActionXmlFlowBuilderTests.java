package org.springframework.webflow.builder;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.builder.XmlFlowBuilderTests.TestFlowArtifactFactory;

public class PojoActionXmlFlowBuilderTests extends TestCase {
	private Flow flow;
	
	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("pojoActionFlow.xml", XmlFlowBuilderTests.class),
				new TestFlowArtifactFactory());
		new FlowAssembler("pojoActionFlow", builder).assembleFlow();
		flow = builder.getResult();
	}
	
	public void testActionStateConfiguration() {
		ActionState as1 = (ActionState)flow.getRequiredState("actionState1");
		assertEquals(ScopeType.REQUEST, as1.getActionList().getAnnotated(0).getResultScope());

		ActionState as2 = (ActionState)flow.getRequiredState("actionState2");
		assertEquals(ScopeType.FLOW, as2.getActionList().getAnnotated(0).getResultScope());
		
		ActionState as3 = (ActionState)flow.getRequiredState("actionState3");
		assertEquals(ScopeType.CONVERSATION, as3.getActionList().getAnnotated(0).getResultScope());

	}

}
