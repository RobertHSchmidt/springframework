package org.springframework.binding.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class AttributeMapAccessorSupportTests extends TestCase {
	private AttributeMapAccessorSupport tested = new AttributeMapAccessorSupport();
	
	public void testGetBoolean() {
		Map attributes = new HashMap();
		attributes.put("boolean", Boolean.TRUE);
		boolean result = tested.getBooleanAttribute("boolean", attributes, false);
		assertEquals(true, result);
	}
}
