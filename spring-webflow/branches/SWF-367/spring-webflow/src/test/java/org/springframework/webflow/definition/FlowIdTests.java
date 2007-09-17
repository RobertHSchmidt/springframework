package org.springframework.webflow.definition;

import junit.framework.TestCase;

public class FlowIdTests extends TestCase {
	public void testNewFlowId() {
		FlowId id = new FlowId("namespace", "short name");
		assertEquals("namespace", id.getNamespace());
		assertEquals("short name", id.getShortName());
	}

	public void testEquals() {
		FlowId id = new FlowId("namespace", "short name");
		FlowId other = new FlowId("namespace", "short name");
		assertEquals(id, other);
		assertEquals(id.hashCode(), other.hashCode());
	}

	public void testValueOf() {
		FlowId id = FlowId.valueOf("short name");
		assertEquals("", id.getNamespace());
		assertEquals("short name", id.getShortName());
	}

	public void testValueOfWithNamespace() {
		FlowId id = FlowId.valueOf("/namespace/short name");
		assertEquals("namespace", id.getNamespace());
		assertEquals("short name", id.getShortName());
	}

	public void testValueOfWithDefaultNamespace() {
		FlowId id = FlowId.valueOf("/short name");
		assertEquals("", id.getNamespace());
		assertEquals("short name", id.getShortName());
	}

	public void testValueOfWithNoShortName() {
		try {
			FlowId.valueOf("/namespace/");
			fail("Should have failed");
		} catch (IllegalArgumentException e) {

		}
	}
}
