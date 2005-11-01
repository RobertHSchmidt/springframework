package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionImpl;

public class TransitionExecutorStateExceptionHandlerTests extends TestCase {
	public void testExceptionHandlingSuccess() {
		Flow flow = new Flow("myFlow");
		State state1 = new TransitionableState(flow, "exception", new Transition[] { new Transition("end") }) {
			protected ViewSelection doEnter(StateContext context) {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		State state2 = new EndState(flow, "end", new SimpleViewSelector("view"));

		TransitionExecutorStateExceptionHandler handler = new TransitionExecutorStateExceptionHandler();
		handler.add(new ExceptionStateMapping(MyCustomException.class, state2));
		StateException e = new StateException(state2, "Oops", new MyCustomException());
		assertTrue("Doesn't handle exception", handler.handles(e));

		flow.addExceptionHandler(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		execution.start(new Event(this, "start"));
		assertTrue("Should have ended", !execution.isActive());
	}
}
