package org.springframework.faces.webflow;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jboss.el.ExpressionFactoryImpl;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.MethodSignature;
import org.springframework.binding.method.Parameter;
import org.springframework.faces.el.Jsf11ELExpressionParser;
import org.springframework.faces.webflow.el.DelegatingFlowVariableResolver;
import org.springframework.faces.webflow.el.RequestContextPropertyResolver;
import org.springframework.faces.webflow.el.RequestContextVariableResolver;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.EventIdTransitionCriteria;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.execution.factory.FlowExecutionKeyFactory;
import org.springframework.webflow.test.MockExternalContext;

public class JSFFlowExecutionTests extends TestCase {

	JSFMockHelper jsf;
	JSFManagedBean jsfBean;
	JSFModel jsfModel;
	MockViewHandler viewHandler;
	MockService service;
	GenericWebApplicationContext ctx;

	Flow flow;
	FlowExecution execution;

	ExpressionParser parser = new Jsf11ELExpressionParser(new ExpressionFactoryImpl());

	protected void setUp() throws Exception {
		service = (MockService) EasyMock.createMock(MockService.class);

		jsfRequestSetup();

		flow = Flow.create("jsf-flow", null);

		ViewState view1 = new ViewState(flow, "viewState1", new JsfViewFactory(jsf.facesContextFactory(),
				new TestLifecycle(jsf.lifecycle()), "view1"));
		view1.getTransitionSet().add(new Transition(on("event1"), to("doSomething")));
		view1.getTransitionSet().add(new Transition(on("event2"), to("evalSomething")));

		ActionState doSomething = new ActionState(flow, "doSomething");
		doSomething.getActionList().add(
				new StubBeanAction(new MethodSignature("doSomething", new Parameter(String.class, parser
						.parseExpression("#{JsfBean.prop1}")))));
		doSomething.getTransitionSet().add(new Transition(on("success"), to("viewState2")));

		ActionState evalSomething = new ActionState(flow, "evalSomething");
		evalSomething.getEntryActionList().add(
				new SetAction(parser.parseSettableExpression("#{requestContext.flowScope.jsfModel}"), ScopeType.FLOW,
						parser.parseExpression("#{'foo'}")));
		evalSomething.getActionList().add(new EvaluateAction(parser.parseExpression("#{JsfBean.addValue(jsfModel)}")));
		evalSomething.getTransitionSet().add(new Transition(on("success"), to("viewState2")));

		ViewState viewState2 = new ViewState(flow, "viewState2", new JsfViewFactory(jsf.facesContextFactory(),
				new TestLifecycle(jsf.lifecycle()), "view2"));
		viewState2.getTransitionSet().add(new Transition(on("event1"), to("endState1")));

		new EndState(flow, "endState1");

		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		factory.setExecutionKeyFactory(new SimpleFlowExecutionKeyFactory());
		execution = factory.createFlowExecution(flow);
	}

	private void jsfRequestSetup() throws Exception {
		jsf = new JSFMockHelper();
		jsf.setUp();
		viewHandler = new NoRenderViewHandler();
		jsf.application().setViewHandler(viewHandler);

		DelegatingFlowVariableResolver dfvr = new DelegatingFlowVariableResolver(jsf.application()
				.getVariableResolver());
		RequestContextVariableResolver fvr = new RequestContextVariableResolver(dfvr);
		jsf.application().setVariableResolver(fvr);
		RequestContextPropertyResolver fpr = new RequestContextPropertyResolver(jsf.application().getPropertyResolver());
		jsf.application().setPropertyResolver(fpr);

		jsf.externalContext().getRequestMap().put("JsfBean", new JSFManagedBean());
	}

	protected void tearDown() throws Exception {
		jsf.tearDown();
	}

	public void testManagedBeanExpression() {
		ValueBinding vb = jsf.application().createValueBinding("#{JsfBean}");
		jsfBean = (JSFManagedBean) vb.getValue(jsf.facesContext());
		assertNotNull(jsfBean);
	}

	public void testManagedBeanPropertyAsArgument() throws Exception {
		startFlow();

		jsfRequestSetup();

		testManagedBeanExpression();
		jsfBean.setProp1("arg");
		service.doSomething(jsfBean.getProp1());
		EasyMock.replay(new Object[] { service });

		jsf.externalContext().getRequestMap().put(JsfView.EVENT_KEY, "event1");

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId("view1");
		viewHandler.setRestoreView(existingRoot);

		execution.resume(new MockExternalContext());

		EasyMock.verify(new Object[] { service });

		ViewState currentState = (ViewState) execution.getActiveSession().getState();
		assertEquals("viewState2", currentState.getId());
	}

	public void testEvalManagedBeanMethod() throws Exception {
		startFlow();

		jsfRequestSetup();

		testManagedBeanExpression();

		jsf.externalContext().getRequestMap().put(JsfView.EVENT_KEY, "event2");

		UIViewRoot existingRoot = new UIViewRoot();
		existingRoot.setViewId("view1");
		viewHandler.setRestoreView(existingRoot);

		execution.resume(new MockExternalContext());

		assertFalse(jsfBean.getValues().isEmpty());
		String addedValue = jsfBean.getValues().get(0).toString();
		assertEquals(addedValue, "foo");

		ViewState currentState = (ViewState) execution.getActiveSession().getState();
		assertEquals("viewState2", currentState.getId());
	}

	private static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	private static TargetStateResolver to(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}

	private void startFlow() {
		UIViewRoot view = new UIViewRoot();
		view.setViewId("view1");
		viewHandler.setCreateView(view);
		execution.start(new MockExternalContext());
	}

	private class TestLifecycle extends FlowLifecycle {

		boolean executed = false;

		public TestLifecycle(Lifecycle delegate) {
			super(delegate);
		}

		public void execute(FacesContext context) throws FacesException {
			assertFalse("Lifecycle executed more than once", executed);
			super.execute(context);
			executed = true;
		}

		public void reset() {
			executed = false;
		}

	}

	private class StubBeanAction extends AbstractBeanInvokingAction {

		protected StubBeanAction(MethodSignature methodSignature) {
			super(methodSignature);
		}

		protected Object getBean(RequestContext context) throws Exception {
			return service;
		}

	}

	private static class SimpleFlowExecutionKeyFactory implements FlowExecutionKeyFactory {
		public FlowExecutionKey getKey(FlowExecution execution) {
			return new FlowExecutionKey() {
				public String toString() {
					return "key";
				}
			};
		}
	}

	private class NoRenderViewHandler extends MockViewHandler {

		public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
			// do nothing
		}
	}
}
