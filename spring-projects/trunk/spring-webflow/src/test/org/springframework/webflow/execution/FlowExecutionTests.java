/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.execution;

import junit.framework.TestCase;

import org.springframework.webflow.ActionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.StateTests.ExecutionCounterAction;
import org.springframework.webflow.StateTests.InputOutputMapper;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.SimpleViewSelector;
import org.springframework.webflow.test.MockExternalContext;

/**
 * General flow execution tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionTests extends TestCase {

	public void testFlowExecutionListener() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", view("mySubFlowViewName"), new Transition[] { new Transition(
				on("submit"), "finish") });
		new EndState(subFlow, "finish");
		subFlow.resolveStateTransitionsTargetStates();

		Flow flow = new Flow("myFlow");
		new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition[] { new Transition(
				on("success"), "viewState") });
		new ViewState(flow, "viewState", view("myView"),
				new Transition[] { new Transition(on("submit"), "subFlowState") });
		new SubflowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition[] { new Transition(
				on("finish"), "finish") });
		new EndState(flow, "finish");
		flow.resolveStateTransitionsTargetStates();

		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		MockFlowExecutionListener flowExecutionListener = new MockFlowExecutionListener();
		flowExecution.getListeners().add(flowExecutionListener);
		flowExecution.start(null, new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(2, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent("submit", null, new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(1, flowExecutionListener.getFlowNestingLevel());
		assertEquals(4, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent("submit", null, new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(6, flowExecutionListener.getTransitionCount());
	}

	public void testLoopInFlow() throws Exception {
		AbstractFlowBuilder builder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addViewState("viewState", "viewName", new Transition[] { on(submit(), "viewState"),
						on(finish(), "endState") });
				addEndState("endState");
			}
		};
		new FlowAssembler("flow", builder).assembleFlow();
		Flow flow = builder.getResult();
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewSelection view = flowExecution.start(null, new MockExternalContext());
		assertNotNull(view);
		assertEquals("viewName", view.getViewName());
		for (int i = 0; i < 10; i++) {
			view = flowExecution.signalEvent("submit", null, new MockExternalContext());
			assertNotNull(view);
			assertEquals("viewName", view.getViewName());
		}
		assertTrue(flowExecution.isActive());
		view = flowExecution.signalEvent("finish", null, new MockExternalContext());
		assertNull(view);
		assertFalse(flowExecution.isActive());
	}

	public void testLoopInFlowWithSubFlow() throws Exception {
		AbstractFlowBuilder childBuilder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addActionState("doOtherStuff", new AbstractAction() {
					private int executionCount = 0;

					protected Event doExecute(RequestContext context) throws Exception {
						executionCount++;
						if (executionCount < 2) {
							return success();
						}
						return error();
					}
				}, new Transition[] { on(success(), finish()), on(error(), "stopTest") });
				addEndState(finish());
				addEndState("stopTest");
			}
		};
		new FlowAssembler("flow", childBuilder).assembleFlow();
		final Flow childFlow = childBuilder.getResult();
		AbstractFlowBuilder parentBuilder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addActionState("doStuff", new AbstractAction() {
					protected Event doExecute(RequestContext context) throws Exception {
						return success();
					}
				}, on(success(), "startSubFlow"));
				addSubflowState("startSubFlow", childFlow, new Transition[] { on(finish(), "startSubFlow"),
						on("stopTest", "stopTest") });
				addEndState("stopTest");
			}
		};
		new FlowAssembler("parentFlow", parentBuilder).assembleFlow();
		Flow parentFlow = parentBuilder.getResult();

		FlowExecution flowExecution = new FlowExecutionImpl(parentFlow);
		flowExecution.start(null, new MockExternalContext());
		assertFalse(flowExecution.isActive());
	}

	public static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	public static ViewSelector view(String viewName) {
		return new SimpleViewSelector(viewName);
	}
}