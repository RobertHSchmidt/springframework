package org.springframework.webflow;

import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

public class NullViewSelectionTests extends TestCase {

	private MockRequestContext context = new MockRequestContext();
	
	public void testMakeSelection() {
		assertEquals(ViewSelection.NULL_VIEW, NullViewSelector.INSTANCE.makeSelection(context));
	}

	public void testMakeRefreshSelection() {
		assertEquals(ViewSelection.NULL_VIEW, NullViewSelector.INSTANCE.makeRefreshSelection(context));
	}
}
