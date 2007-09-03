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

import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.EventIdTransitionCriteria;
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

}