package org.springframework.webflow.execution.repository.support;

import junit.framework.TestCase;

import org.springframework.webflow.conversation.impl.SimpleConversationId;

public class CompositeFlowExecutionKeyTests extends TestCase {
	public void testValidKey() {
		CompositeFlowExecutionKey key = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		assertEquals("_cfoo_kbar", key.toString());
	}

	public void testKeyEquals() {
		CompositeFlowExecutionKey key = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		CompositeFlowExecutionKey key2 = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		assertEquals(key, key2);
	}

}
