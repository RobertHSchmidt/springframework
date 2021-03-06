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
package org.springframework.webflow.engine;

import junit.framework.TestCase;

import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.expression.DefaultExpressionParserFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.EventIdTransitionCriteria;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestControlContext;

/**
 * Tests EndState behavior.
 * @author Keith Donald
 */
public class EndStateTests extends TestCase {

	public void testEnterEndStateTerminateFlowExecution() {
		Flow flow = new Flow("myFlow");
		EndState state = new EndState(flow, "end");
		MockRequestControlContext context = new MockRequestControlContext(flow);
		state.enter(context);
		assertFalse("Active", context.getFlowExecutionContext().isActive());
	}

	public void testEnterEndStateWithFinalResponseRenderer() {
		Flow flow = new Flow("myFlow");
		EndState state = new EndState(flow, "end");
		StubFinalResponseAction action = new StubFinalResponseAction();
		state.setFinalResponseAction(action);
		MockRequestControlContext context = new MockRequestControlContext(flow);
		state.enter(context);
		assertTrue(action.executeCalled);
	}

	public void testEnterEndStateWithOutputMapper() {
		Flow flow = new Flow("myFlow") {
			public void end(RequestControlContext context, MutableAttributeMap output) throws FlowExecutionException {
				assertEquals("foo", output.get("y"));
			}
		};
		EndState state = new EndState(flow, "end");
		DefaultAttributeMapper mapper = new DefaultAttributeMapper();
		MappingBuilder builder = new MappingBuilder(DefaultExpressionParserFactory.getExpressionParser());
		Mapping mapping = builder.source("${flowScope.x}").target("${y}").value();
		mapper.addMapping(mapping);
		state.setOutputMapper(mapper);
		MockRequestControlContext context = new MockRequestControlContext(flow);
		context.getFlowScope().put("x", "foo");
		state.enter(context);
	}

	public void testEnterEndStateTerminateFlowSession() {
		Flow subflow = new Flow("mySubflow");
		EndState state = new EndState(subflow, "end");
		MockFlowSession session = new MockFlowSession(subflow);

		Flow parent = new Flow("parent");
		SubflowState subflowState = new SubflowState(parent, "subflow", subflow);
		subflowState.getTransitionSet().add(new Transition(on("end"), to("end")));
		new EndState(parent, "end");

		MockFlowSession parentSession = new MockFlowSession(parent);
		parentSession.setState(subflowState);

		session.setParent(parentSession);
		MockRequestControlContext context = new MockRequestControlContext(new MockFlowExecutionContext(session));
		state.enter(context);

		assertFalse("Active", context.getFlowExecutionContext().isActive());
	}

	protected static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	protected static TargetStateResolver to(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}

	private class StubFinalResponseAction implements Action {
		private boolean executeCalled;

		public Event execute(RequestContext context) {
			executeCalled = true;
			return new Event(this, "success");
		}
	}
}