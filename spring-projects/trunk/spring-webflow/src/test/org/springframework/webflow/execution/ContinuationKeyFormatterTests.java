package org.springframework.webflow.execution;


import junit.framework.TestCase;

public class ContinuationKeyFormatterTests extends TestCase {
	public void testFormat() {
		FlowExecutionContinuationKey key = new FlowExecutionContinuationKey("abc", "def");
		FlowExecutionContinuationnKeyFormatter formatter = new FlowExecutionContinuationnKeyFormatter();
		String result = formatter.formatValue(key);
		System.out.println(result);
		assertEquals("_sabc_cdef", result);
	}

	public void testParse() {
		FlowExecutionContinuationnKeyFormatter formatter = new FlowExecutionContinuationnKeyFormatter();
		FlowExecutionContinuationKey key = (FlowExecutionContinuationKey)formatter.parseValue("_sabc_cdef", null);
		FlowExecutionContinuationKey key2 = new FlowExecutionContinuationKey("abc", "def");
		assertEquals(key, key2);
	}
}
