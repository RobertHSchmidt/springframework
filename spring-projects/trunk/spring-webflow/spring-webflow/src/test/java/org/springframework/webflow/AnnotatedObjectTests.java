package org.springframework.webflow;

import org.springframework.webflow.engine.AnnotatedObject;
import org.springframework.webflow.engine.Flow;

import junit.framework.TestCase;

public class AnnotatedObjectTests extends TestCase {

	private AnnotatedObject object = new Flow("foo");
	
	public void testSetCaption() {
		object.setCaption("caption");
		assertEquals("caption", object.getCaption());
	}

	public void testSetDescription() {
		object.setDescription("description");
		assertEquals("description", object.getDescription());
	}
	
	public void testPutCustomAttributes() {
		object.getAttributeMap().put("foo", "bar");
		assertEquals("bar", object.getAttributeMap().get("foo"));
	}

}
