package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;

public class RequestPathFlowExecutorArgumentExtractorTests extends TestCase {
	private MockExternalContext context = new MockExternalContext();

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
		try {
			argumentExtractor.extractFlowId(new MockExternalContext());
			fail("should've failed");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testCreateFlowUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		FlowRedirect flowRedirect = new FlowRedirect("flow", null);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows/flow", url);
	}

	public void testCreateFlowExecutionUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		FlowExecutionKey key = new FlowExecutionKey("123", "456");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createFlowExecutionUrl(key, flowExecution, context);
		assertEquals("/app/flows/mockFlow?_flowExecutionKey=_c123_k456", url);
	}

	public void testCreateConversationUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		FlowExecutionKey key = new FlowExecutionKey("123", "456");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createConversationUrl(key, flowExecution, context);
		assertEquals("/app/flows/mockFlow?_conversationId=123", url);
	}

}