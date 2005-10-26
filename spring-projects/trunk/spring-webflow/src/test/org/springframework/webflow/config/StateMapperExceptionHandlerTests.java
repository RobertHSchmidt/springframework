package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionImpl;

public class StateMapperExceptionHandlerTests extends TestCase {
	public void testExceptionHandlingSuccess() {
		Flow flow = new Flow("myFlow");
		State state1 = new State(flow, "exception") {
			protected ViewDescriptor doEnter(StateContext context) {
				throw new StateException(this, "Oops!", new MyCustomException());
			}
		};
		State state2 = new EndState(flow, "end", new SimpleViewDescriptorCreator("view"));

		StateMapperFlowExceptionHandler handler = new StateMapperFlowExceptionHandler();
		handler.add(new ExceptionStateMapping(MyCustomException.class, state2));
		Exception e = new MyCustomException();
		assertTrue("Doesn't handle exception", handler.handles(e));

		flow.addExceptionHandler(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		execution.start(new Event(this, "start"));
		assertTrue("Should have ended", !execution.isActive());
	}
}
