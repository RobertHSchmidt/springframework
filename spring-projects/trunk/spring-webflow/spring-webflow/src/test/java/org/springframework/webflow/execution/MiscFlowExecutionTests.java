package org.springframework.webflow.execution;

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.binding.mapping.RequiredMappingException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.collection.support.LocalAttributeMap;
import org.springframework.webflow.execution.internal.Flow;
import org.springframework.webflow.execution.internal.SubflowState;
import org.springframework.webflow.execution.internal.ViewState;
import org.springframework.webflow.execution.internal.builder.FlowAssembler;
import org.springframework.webflow.execution.internal.builder.xml.XmlFlowBuilder;
import org.springframework.webflow.execution.internal.machine.FlowExecutionImpl;
import org.springframework.webflow.execution.internal.support.ApplicationViewSelector;
import org.springframework.webflow.execution.support.ApplicationView;
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
		LocalAttributeMap input = new LocalAttributeMap();
		input.put("id", "23");
		ApplicationView view = (ApplicationView)execution.start(input, new MockExternalContext());
		assertEquals(new Long(23), view.getModel().get("id"));
	}
	
	public void testRequiredMappingException() {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("required-mapping.xml", getClass()));
		Flow flow = new FlowAssembler("myFlow", builder).assembleFlow();
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
		} catch (RequiredMappingException e) {
			
		}
	}

	/*
	public void testInfiniteLoop() {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("infinite-loop.xml", getClass()));
		Flow flow = new FlowAssembler("myFlow", builder).assembleFlow();
		FlowExecutionImpl execution = new FlowExecutionImpl(flow);
		try {
			execution.start(null, new MockExternalContext());
		} catch (RequiredMappingException e) {
			
		}
	}
	*/
}
