package org.springframework.webflow.action;

import javax.swing.text.html.FormSubmitEvent.MethodType;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.test.MockRequestContext;

public class ReturnValuedBasedEventFactoryTests extends TestCase {
	
	private MockRequestContext context = new MockRequestContext();
	
	private ResultObjectBasedEventFactory factory = new ResultObjectBasedEventFactory();
	
	public void testAlreadyAnEvent() {
		Event event = new Event(this, "event");
		Event result = factory.createResultEvent(this, event, context);
		assertSame(event, result);
	}
	
	public void testMappedTypes() {
		assertTrue(factory.isMappedType(FlowSessionStatus.class));
		assertTrue(factory.isMappedType(MethodType.class));
		assertTrue(factory.isMappedType(Boolean.class));
		assertTrue(factory.isMappedType(String.class));
		assertFalse(factory.isMappedType(Integer.class));
	}
	
	public void testNullResult() {
		Event result = factory.createResultEvent(this, null, context);
		assertEquals("null", result.getId());
	}
	
	public void testBooleanResult() {
		Event result = factory.createResultEvent(this, Boolean.TRUE, context);
		assertEquals("yes", result.getId());
		result = factory.createResultEvent(this, Boolean.FALSE, context);
		assertEquals("no", result.getId());
	}
	
	public void testLabeledEnumResult() {
		Event result = factory.createResultEvent(this, FlowSessionStatus.ACTIVE, context);
		assertEquals("Active", result.getId());
	}
	
	public void testDOtherResult() {
		Event result = factory.createResultEvent(this, "hello", context);
		assertEquals("hello", result.getId());
	}	
}