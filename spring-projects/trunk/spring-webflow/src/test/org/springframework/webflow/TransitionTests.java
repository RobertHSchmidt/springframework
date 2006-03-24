package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.StaticTargetStateResolver;
import org.springframework.webflow.test.MockFlowExecutionControlContext;

public class TransitionTests extends TestCase {

	public void testSimpleTransition() {
		Transition t = new Transition(new StaticTargetStateResolver("target"));
		Flow flow = new Flow();
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.addExitAction(action);
		ViewState target = new ViewState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(t, context.getLastTransition());
		assertEquals(context.getCurrentState(), target);
		assertEquals(1, action.getExecutionCount());
	}
	
	public void testTransitionCriteriaDoesNotMatch() {
		Transition t = new Transition(new EventIdTransitionCriteria("bogus"), new StaticTargetStateResolver("target"));
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(new Flow());
		assertFalse(t.matches(context));
	}
	
	public void testTransitionCannotExecute() {
		Transition t = new Transition(new StaticTargetStateResolver("target"));
		t.setExecutionCriteria(new EventIdTransitionCriteria("bogus"));
		Flow flow = new Flow();
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.addExitAction(action);
		ViewState target = new ViewState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(null, context.getLastTransition());
		assertEquals(context.getCurrentState(), source);
		assertEquals(0, action.getExecutionCount());
	}
}