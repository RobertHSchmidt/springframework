package org.springframework.webflow.executor;

import junit.framework.TestCase;

import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.support.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.StubViewFactory;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorImplTests extends TestCase {
	private FlowDefinitionRegistryImpl definitionLocator;
	private FlowExecutionImplFactory executionFactory;
	private DefaultFlowExecutionRepository executionRepository;
	private FlowExecutorImpl executor;

	protected void setUp() {
		definitionLocator = new FlowDefinitionRegistryImpl();
		executionFactory = new FlowExecutionImplFactory();
		executionRepository = new DefaultFlowExecutionRepository(new SessionBindingConversationManager(),
				new FlowExecutionImplStateRestorer(definitionLocator));
		executionFactory.setExecutionKeyFactory(executionRepository);
		executor = new FlowExecutorImpl(definitionLocator, executionFactory, executionRepository);
	}

	public void testLaunchAndEnd() {
		Flow flow = new Flow("flow");
		new EndState(flow, "end");
		definitionLocator.registerFlowDefinition(new StaticFlowDefinitionHolder(flow));
		MockExternalContext context = new MockExternalContext();
		executor.launchExecution("flow", context);
	}

	public void testLaunchAndResume() {
		Flow flow = new Flow("flow");
		new ViewState(flow, "pause", new StubViewFactory());
		definitionLocator.registerFlowDefinition(new StaticFlowDefinitionHolder(flow));
		MockExternalContext context = new MockExternalContext();
		executor.launchExecution("flow", context);
		// assertTrue(result.isPaused());
		// assertFalse(result.isEnded());
		// assertNotNull(result.getEncodedKey());
		MockExternalContext context2 = new MockExternalContext();
		context2.setSessionMap(context.getSessionMap());
		// executor.resumeExecution(result.getEncodedKey(), context);
	}

	public void testLaunchAndException() {
		Flow flow = new Flow("flow");
		final UnsupportedOperationException e = new UnsupportedOperationException();
		new State(flow, "exception") {
			protected void doEnter(RequestControlContext context) throws FlowExecutionException {
				throw e;
			}
		};
		definitionLocator.registerFlowDefinition(new StaticFlowDefinitionHolder(flow));
		MockExternalContext context = new MockExternalContext();
		executor.launchExecution("flow", context);
		// assertFalse(result.isEnded());
		// assertFalse(result.isPaused());
		// assertNull(result.getEncodedKey());
		// assertTrue(result.isException());
		// assertSame(e, result.getException().getCause());
	}
}
