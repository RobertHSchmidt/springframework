package org.springframework.webflow.execution.repository;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class FlowExecutionKeyTests extends TestCase {
	private FlowExecutionKey key = new FlowExecutionKey("1", "12345");
	
	public void testGetConversationId() {
		assertEquals("1", key.getConversationId());
	}
	
	public void testGetContinuationId() {
		assertEquals("12345", key.getContinuationId());
	}
	
	public void testEqualsHashcode() {
		Map map = new HashMap();
		map.put(key, "foo");
		assertEquals("foo", map.get(key));
	}
}
