package org.springframework.webflow.executor.struts;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.SimpleFlow;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.registry.FlowRegistryImpl;
import org.springframework.webflow.registry.StaticFlowHolder;

public class FlowActionTests extends TestCase {
	private FlowAction controller = new FlowAction();

	private FlowRegistryImpl registry = new FlowRegistryImpl();

	public void setUp() {
		registry.registerFlow(new StaticFlowHolder(new SimpleFlow()));
		controller.setFlowExecutor(new FlowExecutorImpl(registry));
	}

	public void testLaunch() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
	}

	public void testResume() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setContextPath("/app");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
	}
}