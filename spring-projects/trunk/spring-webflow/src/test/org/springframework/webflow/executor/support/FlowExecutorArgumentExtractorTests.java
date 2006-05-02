package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;

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

	public void testExtractFlowIdNoIdProvided() {
		try {
			argumentExtractor.extractFlowId(context);
			fail("no flow id provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testExtractFlowExecutionId() {
		context.putRequestParameter("_flowExecutionKey", "_c12345_k12345");
		assertEquals(flowExecutionKey, argumentExtractor.extractFlowExecutionKey(context));
	}

	public void testExtractFlowExecutionNoKeyProvided() {
		try {
			argumentExtractor.extractFlowExecutionKey(context);
			fail("no flow execution key provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testExtractEventId() {
		context.putRequestParameter("_eventId", "submit");
		assertEquals(new EventId("submit"), argumentExtractor.extractEventId(context));
	}

	public void testExtractEventIdButtonNameFormat() {
		context.putRequestParameter("_eventId_submit", "not important");
		context.putRequestParameter("_somethingElse", "not important");
		assertEquals(new EventId("submit"), argumentExtractor.extractEventId(context));
	}

	public void testExtractEventIdNoIdProvided() {
		try {
			argumentExtractor.extractEventId(context);
			fail("no event id provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testCreateFlowUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowRedirect flowRedirect = new FlowRedirect("flow", null);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows.htm?_flowId=flow", url);
	}

	public void testCreateFlowExecutionUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowExecutionKey key = new FlowExecutionKey("123", "456");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createFlowExecutionUrl(key, flowExecution, context);
		assertEquals("/app/flows.htm?_flowExecutionKey=_c123_k456", url);
	}

	public void testCreateConversationUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowExecutionKey key = new FlowExecutionKey("123", "456");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createConversationUrl(key, flowExecution, context);
		assertEquals("/app/flows.htm?_conversationId=123", url);
	}

	public void testCreateExternalUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowExecutionKey key = new FlowExecutionKey("123", "456");
		ExternalRedirect redirect = new ExternalRedirect("/a/url", false);
		String url = argumentExtractor.createExternalUrl(redirect, key, context);
		assertEquals("/a/url?_flowExecutionKey=_c123_k456", url);
	}

	public void testCreateExternalUrlNoKey() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		ExternalRedirect redirect = new ExternalRedirect("/a/url", true);
		String url = argumentExtractor.createExternalUrl(redirect, null, context);
		assertEquals("/app/a/url", url);
	}

	public void testCreateExternalUrlNoKeyRelativeUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		ExternalRedirect redirect = new ExternalRedirect("a/url", true);
		String url = argumentExtractor.createExternalUrl(redirect, null, context);
		assertEquals("a/url", url);
	}

	public void testAccidentalParameterArraySubmit() {
		context.putRequestParameter("_flowExecutionKey", new String[] { "_c12345_k12345", "_c12345_k12345" });
		assertEquals(flowExecutionKey, argumentExtractor.extractFlowExecutionKey(context));
	}
}