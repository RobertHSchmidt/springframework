package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorArgumentExtractorTests extends TestCase {
	private MockExternalContext context;

	private FlowExecutorArgumentExtractor argumentExtractor;
	
	private FlowExecutionKey flowExecutionKey;

	public void setUp() {
		context = new MockExternalContext();
		argumentExtractor = new FlowExecutorArgumentExtractor();
		flowExecutionKey = new FlowExecutionKey("12345", "12345");
	}

	public void testExtractFlowId() {
		context.putRequestParameter("_flowId", "flow");
		assertEquals("flow", argumentExtractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		argumentExtractor.setDefaultFlowId("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowExecutionId() {
		context.putRequestParameter("_flowExecutionKey", "_s12345_c12345");
		assertEquals(flowExecutionKey, argumentExtractor.extractFlowExecutionKey(context));
	}

	public void testExtractEventId() {
		context.putRequestParameter("_eventId", "submit");
		assertEquals("submit", argumentExtractor.extractEventId(context));
	}

	public void testExtractEventIdButtonNameFormat() {
		context.putRequestParameter("_eventId_submit", "not important");
		assertEquals("submit", argumentExtractor.extractEventId(context));
	}

	public void testAccidentalParameterArraySubmit() {
		context.putRequestParameter("_flowExecutionKey", new String[] { "_s12345_c12345", "_s12345_c12345" });
		try {
			argumentExtractor.extractFlowExecutionKey(context);
			fail("Should've failed");
		}
		catch (IllegalArgumentException e) {

		}
	}
}