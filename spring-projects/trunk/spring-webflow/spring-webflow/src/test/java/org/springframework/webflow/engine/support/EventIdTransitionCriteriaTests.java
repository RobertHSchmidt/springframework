package org.springframework.webflow.engine.support;

import junit.framework.TestCase;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.engine.MockRequestContext;

public class EventIdTransitionCriteriaTests extends TestCase {
	public void testTestCriteria() {
		EventIdTransitionCriteria c = new EventIdTransitionCriteria("foo");
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this, "foo"));
		assertEquals(true, c.test(context));
		context.setLastEvent(new Event(this, "FOO"));
		assertEquals(true, c.test(context));
		context.setLastEvent(new Event(this, "bar"));
		assertEquals(false, c.test(context));
	}
	
	public void testNullLastEventId() {
		EventIdTransitionCriteria c = new EventIdTransitionCriteria("foo");
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(null);
		assertEquals(false, c.test(context));
	}
	
	public void testIllegalArg(){
		try {
			EventIdTransitionCriteria c = new EventIdTransitionCriteria(null);
			fail("was null");
		} catch (IllegalArgumentException e) {
			
		}
		try {
			EventIdTransitionCriteria c = new EventIdTransitionCriteria("");
			fail("was blank");
		} catch (IllegalArgumentException e) {
			
		}
	}
}
