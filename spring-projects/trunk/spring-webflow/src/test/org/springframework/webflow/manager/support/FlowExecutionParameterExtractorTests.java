package org.springframework.webflow.manager.support;

import junit.framework.TestCase;

import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionParameterExtractorTests extends TestCase {
	private FlowExecutorParameterExtractor extractor;

	public void setUp() {
		extractor = new FlowExecutorParameterExtractor();
	}

	public void testExtractFlowId() {
		MockExternalContext context = new MockExternalContext("_flowId", "flow");
		assertEquals("flow", extractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		extractor.setDefaultFlowId("flow");
		assertEquals("flow", extractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowExecutionId() {
		MockExternalContext context = new MockExternalContext("_flowExecutionId", "_s12345_c12345");
		assertEquals("_s12345_c12345", extractor.extractFlowExecutionId(context));
	}

	public void testExtractEventId() {
		MockExternalContext context = new MockExternalContext("_eventId", "submit");
		assertEquals("submit", extractor.extractEventId(context));
	}

	public void testExtractEventIdButtonNameFormat() {
		MockExternalContext context = new MockExternalContext("_eventId_submit", "not important");
		assertEquals("submit", extractor.extractEventId(context));
	}

	public void testAccidentalParameterArraySubmit() {
		MockExternalContext context = new MockExternalContext("_flowExecutionId", new String[] { "_s12345_c12345",
				"_s12345_c12345" });
		try {
			extractor.extractFlowExecutionId(context);
			fail("Should've failed");
		} catch (IllegalArgumentException e) {
			
		}
	}
}