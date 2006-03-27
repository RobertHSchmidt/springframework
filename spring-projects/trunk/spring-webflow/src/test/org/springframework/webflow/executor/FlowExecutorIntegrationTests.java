package org.springframework.webflow.executor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

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
		ResponseInstruction response = flowExecutor.launch("testFlow1", context);
		assertTrue(response.getFlowExecutionContext().isActive());
	}
	
	public void testLaunchAndSignalEvent() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("testFlow1", context);
		response = flowExecutor.signalEvent("event1", response.getFlowExecutionKey(), context);
	}
}
