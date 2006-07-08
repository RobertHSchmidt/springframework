package org.springframework.webflow.execution;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.XmlFlowBuilder;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.test.MockExternalContext;

public class MiscFlowExecutionTests extends TestCase {
	public void testRequestScopePutInEntryAction() {
		Flow parentFlow = new Flow("parent");
		Flow flow = new Flow("test");
		SubflowState parentState = new SubflowState(parentFlow, "parentState", flow);

		ViewState state = new ViewState(flow, "view");
		state.setViewSelector(new ApplicationViewSelector(new StaticExpression("myView")));
		final Object order = new Object();
		state.getEntryActionList().add(new AbstractAction() {
			protected Event doExecute(RequestContext context) {
				context.getRequestScope().put("order", order);
				return success();
			}
		});
		FlowExecution execution = new FlowExecutionImpl(parentFlow);
		ApplicationView response = (ApplicationView)execution.start(null, new MockExternalContext());
		assertNotNull(response.getModel().get("order"));
		assertEquals(order, response.getModel().get("order"));
	}
	
	public void testRequiredMapping() {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("required-mapping.xml", getClass()));
		Flow flow = new FlowAssembler("myFlow", builder).assembleFlow();
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		execution.start(null, new MockExternalContext());
	}
}
