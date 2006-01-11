package org.springframework.webflow.execution.continuation;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.InvalidConversationContinuationException;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuation;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuationFactory;

/**
 * Unit tests for the ContinuationFlowExecutionRepository class.
 * 
 * @author Ulrik Sandberg
 */
public class ContinuationFlowExecutionRepositoryTests extends TestCase {

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockControl flowExecutionContinuationFactoryControl;

	private FlowExecutionContinuationFactory flowExecutionContinuationFactoryMock;

	private MockControl flowExecutionContinuationControl;

	private FlowExecutionContinuation flowExecutionContinuationMock;

	private FlowExecutionContinuationKey key;

	private ContinuationFlowExecutionRepository tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();

		flowExecutionContinuationFactoryControl = MockControl.createControl(FlowExecutionContinuationFactory.class);
		flowExecutionContinuationFactoryMock = (FlowExecutionContinuationFactory)flowExecutionContinuationFactoryControl
				.getMock();

		flowExecutionContinuationControl = MockControl.createControl(FlowExecutionContinuation.class);
		flowExecutionContinuationMock = (FlowExecutionContinuation)flowExecutionContinuationControl.getMock();

		key = new FlowExecutionContinuationKey("some conversation id", "some continuation id");

		tested = new ContinuationFlowExecutionRepository();
		tested.setContinuationFactory(flowExecutionContinuationFactoryMock);
	}

	protected void replay() {
		flowExecutionControl.replay();
		flowExecutionContinuationControl.replay();
		flowExecutionContinuationFactoryControl.replay();
	}

	protected void verify() {
		flowExecutionControl.verify();
		flowExecutionContinuationControl.verify();
		flowExecutionContinuationFactoryControl.verify();
	}

	public void testPutFlowExecution() {
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("some continuation id", flowExecutionMock), flowExecutionContinuationMock);
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getId(), "some continuation id");
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getFlowExecution(),
				flowExecutionMock);

		replay();

		tested.putFlowExecution(key, flowExecutionMock);
		FlowExecution result = tested.getFlowExecution(key);

		verify();
		assertSame(flowExecutionMock, result);
	}

	public void testPutFlowExecutionTwiceWithinSameConversation() {
		FlowExecutionContinuationKey key2 = new FlowExecutionContinuationKey("some conversation id",
				"another continuation id");
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("some continuation id", flowExecutionMock), flowExecutionContinuationMock);
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("another continuation id", flowExecutionMock), flowExecutionContinuationMock);
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getId(), "some continuation id");
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getId(),
				"another continuation id");
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getFlowExecution(),
				flowExecutionMock, 2);

		replay();

		tested.putFlowExecution(key, flowExecutionMock);
		tested.putFlowExecution(key2, flowExecutionMock);
		FlowExecution result = tested.getFlowExecution(key);
		FlowExecution result2 = tested.getFlowExecution(key2);

		verify();
		assertSame("Wrong result for key 'key',", flowExecutionMock, result);
		assertSame("Wrong result for key 'key2',", flowExecutionMock, result2);
	}

	public void testInvalidateConversation() {
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("some continuation id", flowExecutionMock), flowExecutionContinuationMock);

		replay();

		tested.putFlowExecution(key, flowExecutionMock);
		tested.invalidateConversation(key.getConversationId());
		try {
			tested.getFlowExecution(key);
			fail("NoSuchConversationException expected");
		}
		catch (NoSuchConversationException expected) {
			assertEquals(
					"No conversation could be found with id 'some conversation id' -- perhaps this executing flow has ended or expired? This could happen if your users are relying on browser history (typically via the back button) that reference ended flows.",
					expected.getMessage());
		}

		verify();
	}

	public void testGetFlowExecutionWithWrongContinuationId() {
		FlowExecutionContinuationKey invalidKey = new FlowExecutionContinuationKey("some conversation id",
				"wrong continuation id");
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("some continuation id", flowExecutionMock), flowExecutionContinuationMock);
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.getId(), "some continuation id");

		replay();

		tested.putFlowExecution(key, flowExecutionMock);
		try {
			tested.getFlowExecution(invalidKey);
			fail("InvalidConversationContinuationException expected");
		}
		catch (InvalidConversationContinuationException expected) {
			assertEquals(
					"The continuation id 'wrong continuation id' associated with conversation 'some conversation id' is invalid.  This could happen if your users are relying on browser history (typically via the back button) that reference obsoleted or expired continuations.",
					expected.getMessage());
		}

		verify();
	}
}