package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockExternalContext;

public class RequestPathFlowExecutorArgumentExtractorTests extends TestCase {
	private RequestPathFlowExecutorArgumentExtractor argumentExtractor;

	public void setUp() {
		argumentExtractor = new RequestPathFlowExecutorArgumentExtractor();
	}

	public void testExtractFlowId() {
		MockExternalContext context = new MockExternalContext();
		context.setRequestPathInfo("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		argumentExtractor.setDefaultFlowId("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowIdNoRequestPath() {
		assertEquals(null, argumentExtractor.extractFlowId(new MockExternalContext()));
	}
}