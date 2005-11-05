package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.State;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionImpl;

public class TransitionExecutingStateExceptionHandlerTests extends TestCase {

	public void testTransitionExecutorHandlesException() {
		Flow flow = new Flow("myFlow");
		State state1 = new TransitionableState(flow, "exception", new Transition[] { new Transition("end") }) {
			protected ViewSelection doEnter(FlowExecutionControlContext context) {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(new ExceptionStateMapping(MyCustomException.class, state1));
		StateException e = new StateException(state1, "Oops", new MyCustomException());
		assertTrue("Doesn't handle exception", handler.handles(e));
	}

	public void testFlowStateExceptionHandlingTransition() {
		Flow flow = new Flow("myFlow");
		new TransitionableState(flow, "exception", new Transition[] { new Transition("end") }) {
			protected ViewSelection doEnter(FlowExecutionControlContext context) {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		State state2 = new EndState(flow, "end", new SimpleViewSelector("view"));
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(new ExceptionStateMapping(MyCustomException.class, state2));
		flow.addExceptionHandler(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		execution.start(new Event(this, "start"));
		assertTrue("Should have ended", !execution.isActive());
	}

	public void testStateExceptionHandlingTransition() {
		Flow flow = new Flow("myFlow");
		State state1 = new TransitionableState(flow, "exception", new Transition[] { new Transition("end") }) {
			protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		State state2 = new EndState(flow, "end", new SimpleViewSelector("view"));
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(new ExceptionStateMapping(MyCustomException.class, state2));
		state1.addExceptionHandler(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		execution.start(new Event(this, "start"));
		assertTrue("Should have ended", !execution.isActive());
	}

	public void testStateExceptionHandlingRethrow() {
		Flow flow = new Flow("myFlow");
		new TransitionableState(flow, "exception", new Transition[] { new Transition("end") }) {
			protected ViewSelection doEnter(FlowExecutionControlContext context) {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(new Event(this, "start"));
			fail("Should have rethrown");
		}
		catch (StateException e) {
			// expected
		}
	}
}