package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ConversationRedirectTests extends TestCase {
	public void testConstructAndAccess() {
		Map model = new HashMap();
		model.put("name", "value");
		ConversationRedirect r = new ConversationRedirect(new ApplicationView("view", model));
		assertEquals("view", r.getApplicationView().getViewName());
		assertEquals(1, r.getApplicationView().getModel().size());
	}
	
	public void testNullParams() {
		try {
			ConversationRedirect r = new ConversationRedirect(null);
			fail("not null");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testMapLookup() {
		ConversationRedirect r = new ConversationRedirect(new ApplicationView("view", null));
		Map map = new HashMap();
		map.put("view", r);
		assertSame(r, map.get("view"));
	}
}
