package org.springframework.webflow.engine;

import junit.framework.TestCase;

import org.springframework.webflow.TestAction;
import org.springframework.webflow.engine.support.EventIdTransitionCriteria;
import org.springframework.webflow.test.engine.MockRequestControlContext;

public class TransitionTests extends TestCase {

	public void testSimpleTransition() {
		Transition t = new Transition("target");
		Flow flow = new Flow("flow");
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.getExitActionList().add(action);
		ViewState target = new ViewState(flow, "target");
		MockRequestControlContext context = new MockRequestControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(t, context.getLastTransition());
		assertEquals(context.getCurrentState(), target);
		assertEquals(1, action.getExecutionCount());
	}

	public void testTransitionCriteriaDoesNotMatch() {
		Transition t = new Transition(new EventIdTransitionCriteria("bogus"), "target");
		MockRequestControlContext context = new MockRequestControlContext(new Flow("flow"));
		assertFalse(t.matches(context));
	}

	public void testTransitionCannotExecute() {
		Transition t = new Transition("target");
		t.setExecutionCriteria(new EventIdTransitionCriteria("bogus"));
		Flow flow = new Flow("flow");
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.getExitActionList().add(action);
		ViewState target = new ViewState(flow, "target");
		MockRequestControlContext context = new MockRequestControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(null, context.getLastTransition());
		assertEquals(context.getCurrentState(), source);
		assertEquals(0, action.getExecutionCount());
	}
}