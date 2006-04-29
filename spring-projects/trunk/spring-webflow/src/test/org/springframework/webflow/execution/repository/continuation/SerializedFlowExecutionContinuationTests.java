/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.impl.FlowExecutionListeners;

/**
 * Unit tests for the SerializedFlowExecutionContinuation class.
 * 
 * @author Ulrik Sandberg
 */
public class SerializedFlowExecutionContinuationTests extends TestCase {

	private FlowExecutionByteArray flowExecutionByteArray;

	private SerializedFlowExecutionContinuation tested;

	protected void setUp() throws Exception {
		MockFlowExecution mockFlowExecution = new MockFlowExecution();
		flowExecutionByteArray = new FlowExecutionByteArray(mockFlowExecution, false);
		tested = new SerializedFlowExecutionContinuation("some id", flowExecutionByteArray);
	}

	public void testGetFlowExecution() {
		FlowExecution result = tested.getFlowExecution();
		assertNotNull(result);
		assertEquals("This is a mock FlowExecution", result.getCaption());
	}

	public void testToByteArray() {
		byte[] result = tested.toByteArray();
		assertNotNull(result);
	}

	public void testHashCode() {
		int result = tested.hashCode();
		assertEquals("some id".hashCode(), result);
	}

	public void testGetId() {
		Serializable result = tested.getId();
		assertEquals("some id", result);
	}

	public void testEqualsObject() {
		boolean result = tested.equals(tested);
		assertTrue(result);
	}

	/**
	 * Testable implementation of FlowExecution. Must be static in order to stop
	 * serialization from trying to include the test case itself.
	 */
	private static class MockFlowExecution implements FlowExecution, Serializable {

		public AttributeMap getScope() throws IllegalStateException {
			return null;
		}

		public ViewSelection start(AttributeMap input, ExternalContext externalContext) throws StateException {
			return null;
		}

		public ViewSelection signalEvent(EventId eventId, ExternalContext externalContext) throws StateException {
			return null;
		}

		public ViewSelection refresh(ExternalContext context) throws StateException {
			return null;
		}

		public FlowExecutionListeners getListeners() {
			return null;
		}

		public Flow getFlow() {
			return null;
		}

		public Flow getActiveFlow() throws IllegalStateException {
			return null;
		}

		public State getCurrentState() throws IllegalStateException {
			return null;
		}

		public FlowSession getActiveSession() throws IllegalStateException {
			return null;
		}

		public String getCaption() {
			return "This is a mock FlowExecution";
		}

		public boolean isActive() {
			return false;
		}

		public boolean isRootFlowActive() {
			return false;
		}		
	}
}