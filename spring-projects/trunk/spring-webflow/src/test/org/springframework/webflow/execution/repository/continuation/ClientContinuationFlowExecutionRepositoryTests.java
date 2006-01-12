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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base64;
import org.easymock.MockControl;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.continuation.ClientContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuation;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuationFactory;

/**
 * Unit tests for the ClientContinuationFlowExecutionRepository class.
 * 
 * @author Ulrik Sandberg
 */
public class ClientContinuationFlowExecutionRepositoryTests extends TestCase {

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockControl flowExecutionContinuationFactoryControl;

	private FlowExecutionContinuationFactory flowExecutionContinuationFactoryMock;

	private MockControl flowExecutionContinuationControl;

	private FlowExecutionContinuation flowExecutionContinuationMock;

	private ClientContinuationFlowExecutionRepository tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();

		flowExecutionContinuationFactoryControl = MockControl.createControl(FlowExecutionContinuationFactory.class);
		flowExecutionContinuationFactoryMock = (FlowExecutionContinuationFactory)flowExecutionContinuationFactoryControl
				.getMock();

		flowExecutionContinuationControl = MockControl.createControl(FlowExecutionContinuation.class);
		flowExecutionContinuationMock = (FlowExecutionContinuation)flowExecutionContinuationControl.getMock();

		tested = new ClientContinuationFlowExecutionRepository();
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

	public void testEncode() {
		flowExecutionContinuationFactoryControl.expectAndReturn(flowExecutionContinuationFactoryMock
				.createContinuation("some continuation id", flowExecutionMock), flowExecutionContinuationMock);
		flowExecutionContinuationControl.expectAndReturn(flowExecutionContinuationMock.toByteArray(), "some bytes"
				.getBytes());

		replay();

		Serializable result = tested.encode("some continuation id", flowExecutionMock);

		verify();
		String expected = new String(Base64.encodeBase64("some bytes".getBytes()));
		assertEquals(expected, result);
	}

	public void testDecode() throws Exception {
		FlowExecutionContinuation flowExecutionContinuation = new TestableFlowExecutionContinuation();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(flowExecutionContinuation);
		objectOutputStream.close();

		String data = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));

		replay();

		FlowExecutionContinuation result = tested.decode(data);

		verify();
		assertEquals(flowExecutionContinuation, result);
	}

	private static class TestableFlowExecutionContinuation implements FlowExecutionContinuation {

		public Serializable getId() {
			return "some id";
		}

		public FlowExecution getFlowExecution() {
			return null;
		}

		public byte[] toByteArray() {
			return null;
		}

		public boolean equals(Object obj) {
			TestableFlowExecutionContinuation that = (TestableFlowExecutionContinuation)obj;
			return getId().equals(that.getId());
		}
	}
}