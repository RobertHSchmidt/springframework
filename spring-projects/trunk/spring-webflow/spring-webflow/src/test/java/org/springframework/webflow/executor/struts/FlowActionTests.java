package org.springframework.webflow.executor.struts;

import junit.framework.TestCase;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.SimpleFlow;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutorImpl;

public class FlowActionTests extends TestCase {
	private FlowAction action;

	public void setUp() {
		action = new FlowAction() {
			protected WebApplicationContext initWebApplicationContext(ActionServlet actionServlet) throws IllegalStateException {
				StaticWebApplicationContext context = new StaticWebApplicationContext();
				context.setServletContext(new MockServletContext());
				return context;
			}
		};
		
		FlowDefinitionRegistryImpl registry = new FlowDefinitionRegistryImpl();
		registry.registerFlowDefinition(new StaticFlowDefinitionHolder(new SimpleFlow()));
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		FlowExecutionRepository repository = new DefaultFlowExecutionRepository(new FlowExecutionImplStateRestorer(
				registry));
		action.setFlowExecutor(new FlowExecutorImpl(registry, factory, repository));		

		action.setServlet(new ActionServlet());
	}

	public void testLaunch() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
		ActionMapping mapping = new ActionMapping();
		mapping.addForwardConfig(new ActionForward("view", "/view.jsp", false));
		ActionForm form = new SpringBindingActionForm();
		ActionForward forward = action.execute(mapping, form, request, response);
		assertEquals("view", forward.getName());
	}

	public void testResume() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setContextPath("/app");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("_flowId", "simpleFlow");
	}
}