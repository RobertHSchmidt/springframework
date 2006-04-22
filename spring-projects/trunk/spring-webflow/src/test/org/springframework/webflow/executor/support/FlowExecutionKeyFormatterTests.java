package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.binding.format.InvalidFormatException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;

public class FlowExecutionKeyFormatterTests extends TestCase {
	public void testFormat() {
		FlowExecutionKey key = new FlowExecutionKey("abc", "def");
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		String result = formatter.formatValue(key);
		assertEquals("_cabc_kdef", result);
	}

	public void testFormatMultipleDelimiters() {
		FlowExecutionKey key = new FlowExecutionKey("_ab_c_", "_d_e_f");
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		String result = formatter.formatValue(key);
		assertEquals("_c_ab_c__k_d_e_f", result);
	}

	public void testParse() {
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		FlowExecutionKey key = (FlowExecutionKey)formatter.parseValue("_cabc_kdef", null);
		FlowExecutionKey key2 = new FlowExecutionKey("abc", "def");
		assertEquals(key, key2);
	}
	
	public void testParseMultipleDelimiters() {
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		FlowExecutionKey key = (FlowExecutionKey)formatter.parseValue("_c_ab_c__k_d_e_f", null);
		FlowExecutionKey key2 = new FlowExecutionKey("_ab_c_", "_d_e_f");
		assertEquals(key, key2);
	}
	
	public void testParseInvalid() {
		FlowExecutionKeyFormatter formatter = new FlowExecutionKeyFormatter();
		try {
			formatter.parseValue("yery_ycabc_hgj_kdef", null);
			fail("Invalid format not detected");
		} catch (InvalidFormatException e) {
			assertEquals(e.getInvalidValue(), "yery_ycabc_hgj_kdef");
		}
		try {
			formatter.parseValue("_cyery_ycabc_hgj_cdef", null);
			fail("Invalid format not detected");
		} catch (InvalidFormatException e) {
			assertEquals(e.getInvalidValue(), "_cyery_ycabc_hgj_cdef");
		}
		try {
			formatter.parseValue(null, null);
			fail("Illegal arg not detected");
		} catch (IllegalArgumentException e) {
			
		}
		try {
			formatter.parseValue("", null);
			fail("Illegal arg not detected");
		} catch (IllegalArgumentException e) {
			
		}
	}
}