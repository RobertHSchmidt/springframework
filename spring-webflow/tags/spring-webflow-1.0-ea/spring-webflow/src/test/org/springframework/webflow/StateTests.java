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
package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AttributeMapperAction;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.StaticTargetStateResolver;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockParameterMap;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	public void testActionStateSingleAction() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState");
		state.addAction(new ExecutionCounterAction());
		state.addTransition(new Transition(on("success"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		flowExecution.start(new MockExternalContext());
		assertEquals(1, ((ExecutionCounterAction)state.getActionList().get(0)).getExecutionCount());
	}

	public void testActionAttributesChain() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState");
		state.addAction(new ExecutionCounterAction("not mapped result"));
		state.addAction(new ExecutionCounterAction(null));
		state.addAction(new ExecutionCounterAction(""));
		state.addAction(new ExecutionCounterAction("success"));
		state.addTransition(new Transition(on("success"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		flowExecution.start(new MockExternalContext());
		Action[] actions = state.getActionList().toArray();
		for (int i = 0; i < actions.length; i++) {
			ExecutionCounterAction action = (ExecutionCounterAction)actions[i];
			assertEquals(1, action.getExecutionCount());
		}
	}

	public void testActionAttributesChainNoMatchingTransition() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState");
		state.addAction(new ExecutionCounterAction("not mapped result"));
		state.addAction(new ExecutionCounterAction(null));
		state.addAction(new ExecutionCounterAction(""));
		state.addAction(new ExecutionCounterAction("yet another not mapped result"));
		state.addTransition(new Transition(on("success"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		try {
			flowExecution.start(new MockExternalContext());
			fail("Should not have matched to another state transition");
		}
		catch (NoMatchingTransitionException e) {
			// expected
		}
	}

	public void testActionAttributesChainNamedActions() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState");
		state.addAction(new AnnotatedAction(new ExecutionCounterAction("not mapped result")));
		state.addAction(new AnnotatedAction(new ExecutionCounterAction(null)));
		AnnotatedAction action3 = new AnnotatedAction(new ExecutionCounterAction(""));
		action3.setName("action3");
		state.addAction(action3);
		AnnotatedAction action4 = new AnnotatedAction(new ExecutionCounterAction("success"));
		action4.setName("action4");
		state.addAction(action4);
		state.addTransition(new Transition(on("action4.success"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		flowExecution.start(new MockExternalContext());
		assertTrue(!flowExecution.isActive());
		Action[] actions = state.getActionList().toArray();
		for (int i = 0; i < actions.length; i++) {
			AnnotatedAction action = (AnnotatedAction)actions[i];
			assertEquals(1, ((ExecutionCounterAction)(action.getTargetAction())).getExecutionCount());
		}
	}

	public void testViewState() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState");
		state.setViewSelector(view("myViewName"));
		state.addTransition(new Transition(on("submit"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ApplicationView view = (ApplicationView)flowExecution.start(new MockExternalContext());
		assertEquals("viewState", flowExecution.getActiveSession().getState().getId());
		assertNotNull(view);
		assertEquals("myViewName", view.getViewName());
	}

	public void testViewStateMarker() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState");
		state.addTransition(new Transition(on("submit"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewSelection view = flowExecution.start(new MockExternalContext());
		assertEquals("viewState", flowExecution.getActiveSession().getState().getId());
		assertEquals(ViewSelection.NULL_VIEW, view);
	}

	public void testSubFlowState() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState state1 = new ViewState(subFlow, "subFlowViewState");
		state1.setViewSelector(view("mySubFlowViewName"));
		state1.addTransition(new Transition(on("submit"), to("finish")));
		new EndState(subFlow, "finish");

		Flow flow = new Flow("myFlow");
		SubflowState state2 = new SubflowState(flow, "subFlowState", subFlow);
		state2.addTransition(new Transition(on("finish"), to("finish")));

		EndState state3 = new EndState(flow, "finish");
		state3.setViewSelector(view("myParentFlowEndingViewName"));

		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ApplicationView view = (ApplicationView)flowExecution.start(new MockExternalContext());
		assertEquals("mySubFlow", flowExecution.getActiveSession().getFlow().getId());
		assertEquals("subFlowViewState", flowExecution.getActiveSession().getState().getId());
		assertEquals("mySubFlowViewName", view.getViewName());
		view = (ApplicationView)flowExecution.signalEvent("submit", new MockExternalContext());
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
	}

	public void testSubFlowStateModelMapping() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState state1 = new ViewState(subFlow, "subFlowViewState");
		state1.setViewSelector(view("mySubFlowViewName"));
		state1.addTransition(new Transition(on("submit"), to("finish")));
		EndState state2 = new EndState(subFlow, "finish");
		state2.addOutputAttributeName("childInputAttribute");

		Flow flow = new Flow("myFlow");
		ActionState mapperState = new ActionState(flow, "mapperState");
		DefaultAttributeMapper mapper = new DefaultAttributeMapper();
		mapper.addMapping(new MappingBuilder(new OgnlExpressionParser()).source(
				"externalContext.requestParameterMap.parentInputAttribute").target("flowScope.parentInputAttribute")
				.value());
		Action mapperAction = new AttributeMapperAction(mapper);
		mapperState.addAction(mapperAction);
		mapperState.addTransition(new Transition(on("success"), to("subFlowState")));

		SubflowState subflowState = new SubflowState(flow, "subFlowState", subFlow);
		subflowState.setAttributeMapper(new InputOutputMapper());
		subflowState.addTransition(new Transition(on("finish"), to("finish")));

		EndState endState = new EndState(flow, "finish");
		endState.setViewSelector(view("myParentFlowEndingViewName"));

		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		MockParameterMap input = new MockParameterMap();
		input.put("parentInputAttribute", "attributeValue");
		ApplicationView view = (ApplicationView)flowExecution.start(new MockExternalContext(input));
		assertEquals("mySubFlow", flowExecution.getActiveSession().getFlow().getId());
		assertEquals("subFlowViewState", flowExecution.getActiveSession().getState().getId());
		assertEquals("mySubFlowViewName", view.getViewName());
		assertEquals("attributeValue", flowExecution.getActiveSession().getScope().get("childInputAttribute"));
		view = (ApplicationView)flowExecution.signalEvent("submit", new MockExternalContext());
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
		assertEquals("attributeValue", view.getModel().get("parentOutputAttribute"));
	}

	public static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	public static TargetStateResolver to(String stateId) {
		return new StaticTargetStateResolver(stateId);
	}

	public static ViewSelector view(String viewName) {
		return new ApplicationViewSelector(viewName);
	}

	public static class InputOutputMapper implements FlowAttributeMapper {
		public AttributeMap createSubflowInput(RequestContext context) {
			AttributeMap inputMap = new AttributeMap();
			inputMap.put("childInputAttribute", context.getFlowScope().get("parentInputAttribute"));
			return inputMap;
		}

		public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
			AttributeMap parentAttributes = context.getFlowExecutionContext().getActiveSession().getScope();
			parentAttributes.put("parentOutputAttribute", subflowOutput.get("childInputAttribute"));
		}
	}

	public static class ExecutionCounterAction implements Action {
		private Event result = new Event(this, "success");

		private int executionCount;

		public ExecutionCounterAction() {

		}

		public ExecutionCounterAction(String result) {
			if (StringUtils.hasText(result)) {
				this.result = new Event(this, result);
			}
			else {
				this.result = null;
			}
		}

		public int getExecutionCount() {
			return executionCount;
		}

		public Event execute(RequestContext context) throws Exception {
			executionCount++;
			return result;
		}
	}
}
