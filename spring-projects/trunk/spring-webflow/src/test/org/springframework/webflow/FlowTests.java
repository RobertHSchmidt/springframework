package org.springframework.webflow;

import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.SimpleViewDescriptorCreator;

import junit.framework.TestCase;

public class FlowTests extends TestCase {
	public void testAddStates() {
		Flow flow = new Flow("myFlow");
		flow.add(new EndState(flow, "myState1"));
		flow.add(new EndState(flow, "myState2"));
		assertEquals("Wrong start state:", "myState1", flow.getStartState()
				.getId());
		assertEquals("State count wrong:", 2, flow.getStateCount());
		State state = flow.getRequiredState("myState1");
		assertEquals("Wrong flow:", "myFlow", state.getFlow().getId());
		assertEquals("Wrong state:", "myState1", flow.getRequiredState(
				"myState1").getId());
		assertEquals("Wrong state:", "myState2", flow.getState("myState2")
				.getId());
	}

	public void testAddDuplicateState() {
		Flow flow = new Flow("myFlow");
		flow.add(new EndState(flow, "myState1"));
		try {
			flow.add(new EndState(flow, "myState1"));
			fail("Duplicate state added");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testAddStateAlreadyInOtherFlow() {
		Flow otherFlow = new Flow("myOtherFlow");
		State state = new EndState(otherFlow, "myState1");
		otherFlow.add(state);
		Flow flow = new Flow("myFlow");
		try {
			flow.add(state);
			fail("Added state part of another flow");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testGetStates() {
		Flow flow = new Flow("myFlow");
		flow.add(new ViewState(flow, "myState1",
				new SimpleViewDescriptorCreator("myView"), new Transition(
						new EventIdTransitionCriteria("submit"), "myState2")));
		flow.add(new EndState(flow, "myState2"));
	}
}
