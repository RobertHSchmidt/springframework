/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.engine.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.TestException;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.AbstractFlowBuilder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockExternalContext;

public class TransitionExecutingStateExceptionHandlerTests extends TestCase {

	Flow flow;

	TransitionableState state;

	protected void setUp() {
		flow = new Flow("myFlow");
		state = new TransitionableState(flow, "state1") {
			protected ViewSelection doEnter(RequestControlContext context) {
				throw new FlowExecutionException(getFlow().getId(), getId(), "Oops!", new TestException());
			}
		};
		state.getTransitionSet().add(new Transition(toState("end")));
	}

	public void testTransitionExecutorHandlesExceptionExactMatch() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(TestException.class, "state");
		FlowExecutionException e = new FlowExecutionException(state.getOwner().getId(), state.getId(), "Oops",
				new TestException());
		assertTrue("Doesn't handle state exception", handler.handles(e));

		e = new FlowExecutionException(state.getOwner().getId(), state.getId(), "Oops", new Exception());
		assertFalse("Shouldn't handle exception", handler.handles(e));
	}

	public void testTransitionExecutorHandlesExceptionSuperclassMatch() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(Exception.class, "state");
		FlowExecutionException e = new FlowExecutionException(state.getOwner().getId(), state.getId(), "Oops",
				new TestException());
		assertTrue("Doesn't handle state exception", handler.handles(e));
		e = new FlowExecutionException(state.getOwner().getId(), state.getId(), "Oops", new RuntimeException());
		assertTrue("Doesn't handle state exception", handler.handles(e));
	}

	public void testFlowStateExceptionHandlingTransition() {
		EndState state2 = new EndState(flow, "end");
		state2.setViewSelector(new ApplicationViewSelector(new StaticExpression("view")));
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(TestException.class, "end");
		flow.getExceptionHandlerSet().add(handler);
		FlowExecutionListener listener = new FlowExecutionListenerAdapter() {
			public void sessionEnding(RequestContext context, FlowSession session, MutableAttributeMap output) {
				assertTrue(context.getFlashScope().contains("stateException"));
				assertTrue(context.getFlashScope().contains("rootCauseException"));
				assertTrue(context.getFlashScope().get("rootCauseException") instanceof TestException);
			}
		};
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, new FlowExecutionListener[] { listener }, null);
		execution.start(null, new MockExternalContext());
		assertTrue("Should have ended", !execution.isActive());
	}

	public void testStateExceptionHandlingTransitionNoSuchState() {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		handler.add(TestException.class, "end");
		flow.getExceptionHandlerSet().add(handler);
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
			fail("Should have failed no such state");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testStateExceptionHandlingRethrow() {
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
			fail("Should have rethrown");
		} catch (FlowExecutionException e) {
			// expected
		}
	}

	public void testStateExceptionHandlingExceptionInEndState() {
		FlowBuilder builder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				State state = addEndState("end");
				state.getEntryActionList().add(new AbstractAction() {
					protected Event doExecute(RequestContext context) throws Exception {
						throw new NullPointerException("failing");
					}
				});
				addViewState("showError", "error", transition(on("end"), to("end")));
			}

			public void buildExceptionHandlers() throws FlowBuilderException {
				getFlow().getExceptionHandlerSet().add(
						new TransitionExecutingStateExceptionHandler().add(Exception.class, "showError"));
			}
		};
		Flow flow = new FlowAssembler("flow", builder).assembleFlow();
		FlowExecution execution = new FlowExecutionImpl(flow);
		ViewSelection view = execution.start(null, new MockExternalContext());
		assertTrue(execution.isActive());
		assertEquals("error", ((ApplicationView) view).getViewName());
		assertTrue(((ApplicationView) view).getModel().containsKey(
				TransitionExecutingStateExceptionHandler.ROOT_CAUSE_EXCEPTION_ATTRIBUTE));
		assertTrue(((ApplicationView) view).getModel().containsKey(
				TransitionExecutingStateExceptionHandler.STATE_EXCEPTION_ATTRIBUTE));
	}

	protected TargetStateResolver toState(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}
}