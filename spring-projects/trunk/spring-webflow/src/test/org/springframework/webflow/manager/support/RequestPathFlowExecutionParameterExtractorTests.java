package org.springframework.webflow.manager.support;

import junit.framework.TestCase;

import org.springframework.webflow.executor.support.RequestPathFlowExecutorParameterExtractor;
import org.springframework.webflow.test.MockExternalContext;

public class RequestPathFlowExecutionParameterExtractorTests extends TestCase {
	private RequestPathFlowExecutorParameterExtractor extractor;

	public void setUp() {
		extractor = new RequestPathFlowExecutorParameterExtractor();
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