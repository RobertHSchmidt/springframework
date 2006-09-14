package org.springframework.webflow.execution.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.execution.support.LaunchFlowRedirect;

import junit.framework.TestCase;

public class FlowRedirectTests extends TestCase {
	public void testConstructAndAccess() {
		Map input = new HashMap();
		input.put("name", "value");
		LaunchFlowRedirect redirect = new LaunchFlowRedirect("foo", input);
		assertEquals("foo", redirect.getFlowDefinitionId());
		assertEquals(1, redirect.getInput().size());
		assertEquals("value", redirect.getInput().get("name"));
		try {
			redirect.getInput().put("foo", "bar");
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