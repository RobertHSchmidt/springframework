package org.springframework.webflow.executor;


import junit.framework.TestCase;

import org.springframework.webflow.execution.repository.FlowExecutionKey;

public class FlowExecutionKeyFormatterTests extends TestCase {
	public void testFormat() {
		FlowExecutionKey key = new FlowExecutionKey("abc", "def");
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		String result = formatter.formatValue(key);
		assertEquals("_sabc_cdef", result);
	}

	public void testParse() {
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		FlowExecutionKey key = (FlowExecutionKey)formatter.parseValue("_sabc_cdef", null);
		FlowExecutionKey key2 = new FlowExecutionKey("abc", "def");
		assertEquals(key, key2);
	}
}
