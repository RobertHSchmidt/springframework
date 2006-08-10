package org.springframework.webflow.executor.mvc;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.SimpleFlow;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.executor.FlowExecutorImpl;

public class FlowControllerTests extends TestCase {
	private FlowController controller = new FlowController();

	private FlowDefinitionRegistryImpl registry = new FlowDefinitionRegistryImpl();

	public void setUp() {
		registry.registerFlow(new StaticFlowDefinitionHolder(new SimpleFlow()));
		controller.setServletContext(new MockServletContext());
		controller.setFlowExecutor(new FlowExecutorImpl(registry));
	}

	public void testLaunch() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
		ModelAndView mv = controller.handleRequestInternal(request, response);
		assertEquals("view", mv.getViewName());
	}

	public void testResume() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setContextPath("/app");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
		ModelAndView mv = controller.handleRequestInternal(request, response);
		request.addParameter("_flowExecutionKey", (String)mv.getModel().get("flowExecutionKey"));
		request.addParameter("_eventId", "submit");
		mv = controller.handleRequest(request, response);
		assertNull(mv.getViewName());
		assertTrue(mv.getView() instanceof RedirectView);
		RedirectView rv = (RedirectView)mv.getView();
		assertEquals("confirm", rv.getUrl());
		assertNull(mv.getModel().get("flowExecutionKey"));
	}
}