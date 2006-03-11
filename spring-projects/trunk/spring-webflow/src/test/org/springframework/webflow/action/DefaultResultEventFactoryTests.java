package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.webflow.DecisionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.test.MockRequestContext;

public class DefaultResultEventFactoryTests extends TestCase {
	
	private MockRequestContext context = new MockRequestContext();
	
	private DefaultResultEventFactory factory = new DefaultResultEventFactory();
	
	public void testDefaultAdaptionRules() {
		Event result = factory.createResultEvent(this, "result", context);
		assertEquals("success", result.getId());
		assertEquals("result", result.getAttributes().getString("result"));
	}
	
	public void testAlreadyAnEvent() {
		Event event = new Event(this, "event");
		Event result = factory.createResultEvent(this, event, context);
		assertSame(event, result);
	}
	
	public void testDecisionStateNullResult() {
		context.getMockFlowExecutionContext().getMockActiveSession().setState(new DecisionState());
		Event result = factory.createResultEvent(this, null, context);
		assertEquals("null", result.getId());
	}
	
	public void testDecisionStateBooleanResult() {
		context.getMockFlowExecutionContext().getMockActiveSession().setState(new DecisionState());
		Event result = factory.createResultEvent(this, Boolean.TRUE, context);
		assertEquals("yes", result.getId());
		result = factory.createResultEvent(this, Boolean.FALSE, context);
		assertEquals("no", result.getId());
	}
	
	public void testDecisionStateLabeledEnumResult() {
		context.getMockFlowExecutionContext().getMockActiveSession().setState(new DecisionState());
		Event result = factory.createResultEvent(this, FlowSessionStatus.ACTIVE, context);
		assertEquals("Active", result.getId());
	}
	
	public void testDecisionStateOtherResult() {
		context.getMockFlowExecutionContext().getMockActiveSession().setState(new DecisionState());
		Event result = factory.createResultEvent(this, "hello", context);
		assertEquals("hello", result.getId());
	}	
}