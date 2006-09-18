package org.springframework.webflow.execution.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class LaunchFlowRedirectTests extends TestCase {
	public void testConstructAndAccess() {
		Map input = new HashMap();
		input.put("name", "value");
		LaunchFlowRedirect redirect = new LaunchFlowRedirect("foo", input);
		assertEquals("foo", redirect.getFlowDefinitionId());
		assertEquals(1, redirect.getExecutionInput().size());
		assertEquals("value", redirect.getExecutionInput().get("name"));
		try {
			redirect.getExecutionInput().put("foo", "bar");
		} catch (UnsupportedOperationException e) {
			
		}
	}
	
	public void testNullParams() {
		try {
			LaunchFlowRedirect redirect = new LaunchFlowRedirect(null, null);
			fail("was null");
		} catch (IllegalArgumentException e) {
			
		}

	}
	
	public void testMapLookup() {
		LaunchFlowRedirect redirect = new LaunchFlowRedirect("foo", null);
		Map map = new HashMap();
		map.put("redirect", redirect);
		assertSame(redirect, map.get("redirect"));
	}
}