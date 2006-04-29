package org.springframework.webflow.executor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.NoMatchingTransitionException;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.repository.CannotContinueConversationException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorIntegrationTests extends AbstractDependencyInjectionSpringContextTests {

	private FlowExecutor flowExecutor;

	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/webflow/executor/context.xml" };
	}

	public void testConfigurationOk() {
		assertNotNull(flowExecutor);
	}

	public void testLaunchFlow() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("viewState1", response.getFlowExecutionContext().getActiveSession().getState().getId());
		assertTrue(response.isApplicationView());
		ApplicationView view = (ApplicationView)response.getViewSelection();
		assertEquals("view1", view.getViewName());
		assertEquals(0, view.getModel().size());
	}

	public void testLaunchNoSuchFlow() {
		try {
			ExternalContext context = new ServletExternalContext(new MockServletContext(),
					new MockHttpServletRequest(), new MockHttpServletResponse());
			flowExecutor.launch("bogus", context);
			fail("no such flow expected");
		}
		catch (NoSuchFlowDefinitionException e) {
			assertEquals("bogus", e.getArtifactId());
			assertEquals(Flow.class, e.getArtifactType());
		}
	}

	public void testLaunchAndSignalEvent() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		FlowExecutionKey key = response.getFlowExecutionKey();
		assertEquals("viewState1", response.getFlowExecutionContext().getActiveSession().getState().getId());
		response = flowExecutor.signalEvent(new EventId("event1"), key, context);
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("viewState2", response.getFlowExecutionContext().getActiveSession().getState().getId());
		assertTrue(response.isApplicationView());
		assertNotNull(response.getFlowExecutionKey());
		assertEquals(key.getConversationId(), response.getFlowExecutionKey().getConversationId());
		assertFalse(key.getContinuationId() == response.getFlowExecutionKey().getContinuationId());
		ApplicationView view = (ApplicationView)response.getViewSelection();
		assertEquals("view2", view.getViewName());
		assertEquals(0, view.getModel().size());
		response = flowExecutor.signalEvent(new EventId("event1"), response.getFlowExecutionKey(), context);
		view = (ApplicationView)response.getViewSelection();
		assertFalse(response.getFlowExecutionContext().isActive());
		assertTrue(response.isApplicationView());
		assertNull(response.getFlowExecutionKey());
		assertEquals("endView1", view.getViewName());
		assertEquals(0, view.getModel().size());
		try {
			flowExecutor.signalEvent(new EventId("event1"), key, context);
			fail("Should've been removed");
		}
		catch (NoSuchConversationException e) {

		}
	}

	public void testGetCurrentResponseInstruction() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		ResponseInstruction response2 = flowExecutor.refresh(response.getFlowExecutionKey()
				.getConversationId(), context);
		assertEquals(response, response2);
	}

	public void testNoSuchConversation() {
		try {
			flowExecutor.signalEvent(new EventId("bogus"), new FlowExecutionKey("bogus", "bogus"),
					new MockExternalContext());
			fail("Should've failed");
		}
		catch (NoSuchConversationException e) {
			assertEquals("bogus", e.getConversationId());
		}
	}

	public void testCannotContinuationConversation() {
		FlowExecutionKey key = null;
		try {
			ExternalContext context = new ServletExternalContext(new MockServletContext(),
					new MockHttpServletRequest(), new MockHttpServletResponse());
			ResponseInstruction response = flowExecutor.launch("flow", context);
			key = response.getFlowExecutionKey();
			flowExecutor.signalEvent(new EventId("event1"), new FlowExecutionKey(key.getConversationId(), "bogus"),
					context);
			fail("How did you continue mon?");
		}
		catch (CannotContinueConversationException e) {
			assertEquals(key.getConversationId(), e.getFlowExecutionKey().getConversationId());
			assertEquals("bogus", e.getFlowExecutionKey().getContinuationId());
		}
	}

	public void testSignalEventNoMatchingTransition() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		FlowExecutionKey key = response.getFlowExecutionKey();
		try {
			flowExecutor.signalEvent(new EventId("bogus"), key, context);
			fail("Should've been removed");
		}
		catch (NoMatchingTransitionException e) {
			assertEquals("flow", e.getFlow().getId());
			assertEquals("viewState1", e.getState().getId());
			assertEquals("bogus", e.getEvent().getId());
		}
	}

	public void testNoSuchConversationCurrentResponse() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		flowExecutor.launch("flow", context);
		try {
			flowExecutor.refresh("bogus", context);
			fail("Should've failed");
		}
		catch (NoSuchConversationException e) {
			assertEquals("bogus", e.getConversationId());
		}
	}
}