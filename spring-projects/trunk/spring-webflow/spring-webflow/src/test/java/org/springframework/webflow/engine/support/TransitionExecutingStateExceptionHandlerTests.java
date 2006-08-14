package org.springframework.webflow.engine.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.MyCustomException;
import org.springframework.webflow.engine.machine.FlowExecutionImpl;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.TransitionExecutingStateExceptionHandler;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.test.MockExternalContext;

public class TransitionExecutingStateExceptionHandlerTests extends TestCase {

	Flow flow;

	TransitionableState state;

	protected void setUp() {
		flow = new Flow("myFlow");
		state = new TransitionableState(flow, "state1") {
			protected ViewSelection doEnter(RequestControlContext context) {
				throw new FlowExecutionException(getFlow().getId(), getId(), "Oops!", new MyCustomException());
			}
		};
		state.getTransitionSet().add(new Transition(to("end")));
	}

	public void testTransitionExecutorHandlesExceptionExactMatch() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(MyCustomException.class, "state");
		FlowExecutionException e = new FlowExecutionException(state.getFlow().getId(), state.getId(), "Oops", new MyCustomException());
		assertTrue("Doesn't handle state exception", handler.handles(e));

		e = new FlowExecutionException(state.getFlow().getId(), state.getId(), "Oops", new Exception());
		assertFalse("Shouldn't handle exception", handler.handles(e));
	}

	public void testTransitionExecutorHandlesExceptionSuperclassMatch() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(Exception.class, "state");
		FlowExecutionException e = new FlowExecutionException(state.getFlow().getId(), state.getId(), "Oops", new MyCustomException());
		assertTrue("Doesn't handle state exception", handler.handles(e));
		e = new FlowExecutionException(state.getFlow().getId(), state.getId(), "Oops", new RuntimeException());
		assertTrue("Doesn't handle state exception", handler.handles(e));
	}

	public void testFlowStateExceptionHandlingTransition() {
		EndState state2 = new EndState(flow, "end");
		state2.setViewSelector(new ApplicationViewSelector(new StaticExpression("view")));
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(MyCustomException.class, "end");
		flow.getExceptionHandlerSet().add(handler);
		FlowExecutionListener listener = new FlowExecutionListenerAdapter() {
			public void requestProcessed(RequestContext context) {
				assertTrue(context.getRequestScope().contains("stateException"));
				assertTrue(context.getRequestScope().contains("rootCauseException"));
				assertTrue(context.getRequestScope().get("rootCauseException") instanceof MyCustomException);
			}
		};
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, new FlowExecutionListener[] { listener });
		execution.start(null, new MockExternalContext());
		assertTrue("Should have ended", !execution.isActive());
	}

	public void testStateExceptionHandlingTransitionNoSuchState() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(MyCustomException.class, "end");
		flow.getExceptionHandlerSet().add(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
			fail("Should have failed no such state");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testStateExceptionHandlingRethrow() {
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
			fail("Should have rethrown");
		}
		catch (FlowExecutionException e) {
			// expected
		}
	}

	public static TargetStateResolver to(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}
}