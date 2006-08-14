package org.springframework.webflow.executor.mvc;

import junit.framework.TestCase;

import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.SimpleFlow;
import org.springframework.webflow.executor.FlowExecutorImpl;

public class PortletFlowControllerTests extends TestCase {
	private PortletFlowController controller = new PortletFlowController();

	private FlowDefinitionRegistryImpl registry = new FlowDefinitionRegistryImpl();

	public void setUp() {
		registry.registerFlowDefinition(new StaticFlowDefinitionHolder(new SimpleFlow()));
		controller.setPortletContext(new MockPortletContext());
		controller.setFlowExecutor(new FlowExecutorImpl(registry));
	}

	public void testLaunch() throws Exception {
		MockRenderRequest request = new MockRenderRequest();
		MockRenderResponse response = new MockRenderResponse();
		request.addParameter("_flowId", "simpleFlow");
		ModelAndView mv = controller.handleRenderRequest(request, response);
		assertEquals("view", mv.getViewName());
	}

	public void testResume() throws Exception {
		MockRenderRequest renderRequest = new MockRenderRequest();
		MockRenderResponse renderResponse = new MockRenderResponse();
		renderRequest.addParameter("_flowId", "simpleFlow");
		ModelAndView mv = controller.handleRenderRequest(renderRequest, renderResponse);
		assertEquals("view", mv.getViewName());
		assertNotNull(mv.getModel().get("flowExecutionKey"));
		
		MockActionRequest actionRequest = new MockActionRequest();
		actionRequest.setSession(renderRequest.getPortletSession());
		actionRequest.setContextPath("/app");
		MockActionResponse actionResponse = new MockActionResponse();
		actionRequest.addParameter("_flowExecutionKey", (String)mv.getModel().get("flowExecutionKey"));
		actionRequest.addParameter("_eventId", "submit");
		try {
			controller.handleActionRequest(actionRequest, actionResponse);
		} catch (IllegalArgumentException e) {
			
		}
	}
}