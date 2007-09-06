package org.springframework.webflow.executor;

import junit.framework.TestCase;

import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.SimpleFlowExecutionRepository;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorImplTests extends TestCase {
	private FlowDefinitionRegistryImpl definitionLocator;
	private FlowExecutionFactory executionFactory;
	private FlowExecutionRepository executionRepository;
	private FlowExecutorImpl executor;

	protected void setUp() {
		definitionLocator = new FlowDefinitionRegistryImpl();
		executionFactory = new FlowExecutionImplFactory();
		executionRepository = new SimpleFlowExecutionRepository(new FlowExecutionImplStateRestorer(definitionLocator),
				new SessionBindingConversationManager());
		executor = new FlowExecutorImpl(definitionLocator, executionFactory, executionRepository);
	}

	public void testLaunchAndEnd() {
		Flow flow = new Flow("flow");
		new EndState(flow, "end");
		definitionLocator.registerFlowDefinition(new StaticFlowDefinitionHolder(flow));

		MockExternalContext context = new MockExternalContext();
		context.setRequestPathInfo("flow");
		executor.execute(context);
	}

	public void testLaunchAndResume() {
		Flow flow = new Flow("flow");
		new ViewState(flow, "pause", new SimpleViewFactory());
		definitionLocator.registerFlowDefinition(new StaticFlowDefinitionHolder(flow));

		MockExternalContext context = new MockExternalContext();
		context.setRequestPathInfo("flow");
		executor.execute(context);
		// paused

		context = new MockExternalContext();
		context.setRequestPathInfo("/execution/12345");
		executor.execute(context);
	}

	public static class SimpleViewFactory implements ViewFactory {

		public View getView(RequestContext context) {
			return new SimpleView();
		}

		public View restoreView(RequestContext context) {
			return new SimpleView();
		}

		public static class SimpleView extends View {

			public boolean eventSignaled() {
				return false;
			}

			public Event getEvent() {
				return null;
			}

			public void render(RequestContext context) {

			}

		}

	}
}
