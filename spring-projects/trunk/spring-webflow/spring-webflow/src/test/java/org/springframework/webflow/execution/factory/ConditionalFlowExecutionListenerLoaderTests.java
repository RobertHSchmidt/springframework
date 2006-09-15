package org.springframework.webflow.execution.factory;

import junit.framework.TestCase;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;

public class ConditionalFlowExecutionListenerLoaderTests extends TestCase {

	private ConditionalFlowExecutionListenerLoader loader = new ConditionalFlowExecutionListenerLoader();

	protected void setUp() throws Exception {
	}

	public void testAddListener() {
		FlowExecutionListener l1 = new FlowExecutionListenerAdapter() {
		};
		FlowExecutionListener l2 = new FlowExecutionListenerAdapter() {
		};
		loader.addListener(l1);
		assertTrue(loader.containsListener(l1));
		loader.addListener(l2);
		assertTrue(loader.containsListener(l2));
		FlowExecutionListener[] listeners = loader.getListeners(new Flow("foo"));
		assertEquals(2, listeners.length);
		assertSame(l1, listeners[0]);
		assertSame(l2, listeners[1]);
		loader.removeListener(l1);
		assertFalse(loader.containsListener(l1));
		loader.removeListener(l2);
		assertEquals(0, loader.getListeners(new Flow("flow")).length);
	}

	public void testAddListenerWithCriteria() {
		FlowExecutionListener l1 = new FlowExecutionListenerAdapter() {
		};
		FlowExecutionListener l2 = new FlowExecutionListenerAdapter() {
		};
		loader.addListener(l1);
		assertTrue(loader.containsListener(l1));
		assertFalse(loader.containsListener(l2));
		final Flow theFlow = new Flow("foo");
		loader.addListener(l2, new FlowExecutionListenerCriteria() {
			public boolean appliesTo(FlowDefinition flow) {
				assertSame(theFlow, flow);
				return false;
			}
		});
		FlowExecutionListener[] listeners = loader.getListeners(theFlow);
		assertEquals(1, listeners.length);
		assertSame(l1, listeners[0]);
	}

	public void testAddListenerGroup() {
		FlowExecutionListener l1 = new FlowExecutionListenerAdapter() {
		};
		FlowExecutionListener l2 = new FlowExecutionListenerAdapter() {
		};
		FlowExecutionListener l3 = new FlowExecutionListenerAdapter() {
		};
		FlowExecutionListener l4 = new FlowExecutionListenerAdapter() {
		};
		loader.addListener(l1);
		loader.addListener(l2);
		loader.addListeners(new FlowExecutionListener[] { l3, l4 }, new FlowExecutionListenerCriteriaFactory()
				.flow("bogus"));
		assertTrue(loader.containsListener(l1));
		assertTrue(loader.containsListener(l2));
		assertTrue(loader.containsListener(l3));
		assertTrue(loader.containsListener(l4));
		FlowExecutionListener[] listeners = loader.getListeners(new Flow("foo"));
		assertEquals(2, listeners.length);
		assertSame(l1, listeners[0]);
		assertSame(l2, listeners[1]);
	}

	public void testNullFlowDefinition() {
		try {
			loader.getListeners(null);
			fail("Should have failed");
		}
		catch (IllegalArgumentException e) {

		}

	}
}