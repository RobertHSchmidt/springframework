package org.springframework.webflow.core.collection;

import junit.framework.TestCase;

public class CollectionUtilsTests extends TestCase {
	public void testSingleEntryMap() {
		AttributeMap map1 = CollectionUtils.singleEntryMap("foo", "bar");
		AttributeMap map2 = CollectionUtils.singleEntryMap("foo", "bar");
		assertEquals(map1, map2);
	}
}
