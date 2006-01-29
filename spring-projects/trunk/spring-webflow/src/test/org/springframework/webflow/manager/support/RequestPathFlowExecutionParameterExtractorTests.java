package org.springframework.webflow.manager.support;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockExternalContext;

public class RequestPathFlowExecutionParameterExtractorTests extends TestCase {
	private RequestPathFlowExecutionManagerParameterExtractor extractor;

	public void setUp() {
		extractor = new RequestPathFlowExecutionManagerParameterExtractor();
	}

	public void testExtractFlowId() {
		MockExternalContext context = new MockExternalContext();
		context.setRequestPathInfo("flow");
		assertEquals("flow", extractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		extractor.setDefaultFlowId("flow");
		assertEquals("flow", extractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowIdNoRequestPath() {
		assertEquals(null, extractor.extractFlowId(new MockExternalContext()));
	}
}