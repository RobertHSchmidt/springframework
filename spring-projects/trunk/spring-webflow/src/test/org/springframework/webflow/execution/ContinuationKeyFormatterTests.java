package org.springframework.webflow.execution;


import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKeyFormatter;

import junit.framework.TestCase;

public class ContinuationKeyFormatterTests extends TestCase {
	public void testFormat() {
		FlowExecutionContinuationKey key = new FlowExecutionContinuationKey("abc", "def");
		FlowExecutionContinuationKeyFormatter formatter = new FlowExecutionContinuationKeyFormatter();
		String result = formatter.formatValue(key);
		System.out.println(result);
		assertEquals("_sabc_cdef", result);
	}

	public void testParse() {
		FlowExecutionContinuationKeyFormatter formatter = new FlowExecutionContinuationKeyFormatter();
		FlowExecutionContinuationKey key = (FlowExecutionContinuationKey)formatter.parseValue("_sabc_cdef", null);
		FlowExecutionContinuationKey key2 = new FlowExecutionContinuationKey("abc", "def");
		assertEquals(key, key2);
	}
}
