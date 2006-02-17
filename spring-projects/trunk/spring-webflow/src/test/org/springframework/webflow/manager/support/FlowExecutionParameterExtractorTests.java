package org.springframework.webflow.manager.support;

import junit.framework.TestCase;

import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutionParameterExtractorTests extends TestCase {
	private FlowExecutorParameterExtractor extractor;

	private FlowExecutionKey flowExecutionKey;

	public void setUp() {
		extractor = new FlowExecutorParameterExtractor();
		flowExecutionKey = new FlowExecutionKey("12345", "12345");
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
		MockExternalContext context = new MockExternalContext("_flowExecutionKey", "_s12345_c12345");
		assertEquals(flowExecutionKey, extractor.extractFlowExecutionKey(context));
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
		MockExternalContext context = new MockExternalContext("_flowExecutionKey", new String[] { "_s12345_c12345",
				"_s12345_c12345" });
		try {
			extractor.extractFlowExecutionKey(context);
			fail("Should've failed");
		}
		catch (IllegalArgumentException e) {

		}
	}
}