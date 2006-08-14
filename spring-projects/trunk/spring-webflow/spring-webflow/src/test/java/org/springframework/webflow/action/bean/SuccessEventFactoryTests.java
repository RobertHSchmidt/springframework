package org.springframework.webflow.action.bean;

import junit.framework.TestCase;

import org.springframework.webflow.action.bean.SuccessEventFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.engine.MockRequestContext;

public class SuccessEventFactoryTests extends TestCase {

	private MockRequestContext context = new MockRequestContext();

	private SuccessEventFactory factory = new SuccessEventFactory();

	public void testDefaultAdaptionRules() {
		Event result = factory.createResultEvent(this, "result", context);
		assertEquals("success", result.getId());
		assertEquals("result", result.getAttributes().getString("result"));
	}
}