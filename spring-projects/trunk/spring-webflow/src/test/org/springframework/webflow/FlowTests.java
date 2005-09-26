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

import org.springframework.webflow.support.SimpleViewDescriptorCreator;

/**
 * Unit test for the Flow class.
 * 
 * @author Keith Donald
 */
public class FlowTests extends TestCase {

	public void testAddStates() {
		Flow flow = new Flow("myFlow");
		flow.add(new EndState(flow, "myState1"));
		flow.add(new EndState(flow, "myState2"));
		assertEquals("Wrong start state:", "myState1", flow.getStartState().getId());
		assertEquals("State count wrong:", 2, flow.getStateCount());
		State state = flow.getRequiredState("myState1");
		assertEquals("Wrong flow:", "myFlow", state.getFlow().getId());
		assertEquals("Wrong state:", "myState1", flow.getRequiredState("myState1").getId());
		assertEquals("Wrong state:", "myState2", flow.getState("myState2").getId());
	}

	public void testAddDuplicateState() {
		Flow flow = new Flow("myFlow");
		flow.add(new EndState(flow, "myState1"));
		try {
			flow.add(new EndState(flow, "myState1"));
			fail("Duplicate state added");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testAddSameStateTwice() {
		Flow flow = new Flow("myFlow");
		EndState state = new EndState(flow, "myState1");
		flow.add(state);
		flow.add(state);
		assertEquals("State count wrong:", 1, flow.getStateCount());
	}

	public void testAddStateAlreadyInOtherFlow() {
		Flow otherFlow = new Flow("myOtherFlow");
		State state = new EndState(otherFlow, "myState1");
		otherFlow.add(state);
		Flow flow = new Flow("myFlow");
		try {
			flow.add(state);
			fail("Added state part of another flow");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testGetStateExceptions() {
		Flow flow = new Flow("myFlow");
		try {
			flow.getStartState();
			fail("Retrieved start state when no such state");
		}
		catch (IllegalStateException e) {
			// expected
		}
		flow.add(new ViewState(flow, "myState1", new SimpleViewDescriptorCreator("myView"),
				new Transition[] { new Transition("myState2") }));
		flow.add(new EndState(flow, "myState2"));
		assertNull("Not null", flow.getState("myState3"));
		try {
			flow.getRequiredState("myState3");
			fail("Returned a state that doesn't exist");
		}
		catch (NoSuchFlowStateException e) {
			// expected
		}
		assertEquals("Wrong state:", "myState1", flow.getRequiredTransitionableState("myState1").getId());
		try {
			flow.getRequiredTransitionableState("myState2");
			fail("End states aren't transtionable");
		}
		catch (IllegalStateException e) {
			// expected
		}
	}
}