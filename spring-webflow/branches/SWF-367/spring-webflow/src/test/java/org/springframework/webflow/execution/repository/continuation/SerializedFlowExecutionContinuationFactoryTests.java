package org.springframework.webflow.execution.repository.continuation;

import junit.framework.TestCase;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;
import org.springframework.webflow.test.MockExternalContext;

public class SerializedFlowExecutionContinuationFactoryTests extends TestCase {
	private Flow flow;
	private SerializedFlowExecutionContinuationFactory factory;
	private FlowExecutionStateRestorer stateRestorer;

	public void setUp() {
		flow = Flow.create("myFlow");
		new State(flow, "state") {
			protected void doEnter(RequestControlContext context) throws FlowExecutionException {
			}
		};
		factory = new SerializedFlowExecutionContinuationFactory();
		stateRestorer = new FlowExecutionImplStateRestorer(new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(FlowId flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return flow;
			}
		});
	}

	public void testCreateContinuation() {
		FlowExecutionImpl flowExecution = new FlowExecutionImpl(flow);
		flowExecution.start(null, new MockExternalContext());
		flowExecution.getActiveSession().getScope().put("foo", "bar");
		FlowExecutionContinuation continuation = factory.createContinuation(flowExecution);
		FlowExecutionImpl flowExecution2 = (FlowExecutionImpl) continuation.unmarshal();
		assertNotSame(flowExecution, flowExecution2);
		stateRestorer.restoreState(flowExecution2, null, flowExecution.getConversationScope());
		assertEquals(flowExecution.getDefinition().getId(), flowExecution2.getDefinition().getId());
		assertEquals(flowExecution.getActiveSession().getScope().get("foo"), flowExecution2.getActiveSession()
				.getScope().get("foo"));
		assertEquals(flowExecution.getActiveSession().getState().getId(), flowExecution2.getActiveSession().getState()
				.getId());
	}

	public void testRestoreContinuation() {
		FlowExecutionImpl flowExecution = new FlowExecutionImpl(flow);
		flowExecution.start(null, new MockExternalContext());
		flowExecution.getActiveSession().getScope().put("foo", "bar");
		FlowExecutionContinuation continuation = factory.createContinuation(flowExecution);
		byte[] bytes = continuation.toByteArray();
		FlowExecutionContinuation continuation2 = factory.restoreContinuation(bytes);
		assertEquals(continuation, continuation2);
		FlowExecutionImpl flowExecution2 = (FlowExecutionImpl) continuation2.unmarshal();
		assertNotSame(flowExecution, flowExecution2);
		stateRestorer.restoreState(flowExecution2, null, flowExecution.getConversationScope());
		assertEquals(flowExecution.getDefinition().getId(), flowExecution2.getDefinition().getId());
		assertEquals(flowExecution.getActiveSession().getScope().get("foo"), flowExecution2.getActiveSession()
				.getScope().get("foo"));
		assertEquals(flowExecution.getActiveSession().getState().getId(), flowExecution2.getActiveSession().getState()
				.getId());
	}
}