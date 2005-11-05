/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.execution.servlet;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Test case for the ExpiredFlowCleanupFilter class.
 * 
 * @author Ulrik Sandberg
 */
public class ExpiredFlowCleanupFilterTests extends TestCase {

	private MockHttpServletRequest mockRequest;

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private ExpiredFlowCleanupFilter tested;

	protected void setUp() throws Exception {
		super.setUp();
		mockRequest = new MockHttpServletRequest();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();
		tested = new ExpiredFlowCleanupFilter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockRequest = null;
		flowExecutionControl = null;
		flowExecutionMock = null;
		tested = null;
	}

	protected void replay() {
		flowExecutionControl.replay();
	}

	protected void verify() {
		flowExecutionControl.verify();
	}

	public void testDoCleanupNullSession() {
		replay();

		// perform test
		tested.doCleanup(mockRequest);

		verify();
		assertNull("no session should have been created", mockRequest.getSession(false));
	}

	public void testDoCleanupHasExpired() {
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		mockSession.setAttribute("Other", new Object());
		mockSession.setAttribute("SomeFlowExecution", flowExecutionMock);

		flowExecutionControl.expectAndReturn(flowExecutionMock.getLastRequestTimestamp(), 0);
		flowExecutionControl.expectAndReturn(flowExecutionMock.getRootFlow(), new Flow("dummy"));
		replay();

		// perform test
		tested.doCleanup(mockRequest);

		verify();
		assertNotNull("Other should still be left", mockSession.getAttribute("Other"));
		assertNull("SomeFlowExecution should be gone", mockSession.getAttribute("SomeFlowExecution"));
	}

	public void testDoCleanupHasNotExpired() {
		MockHttpSession mockSession = new MockHttpSession();
		mockRequest.setSession(mockSession);
		mockSession.setAttribute("Other", new Object());
		mockSession.setAttribute("SomeFlowExecution", flowExecutionMock);

		flowExecutionControl.expectAndReturn(flowExecutionMock.getLastRequestTimestamp(), System.currentTimeMillis());
		replay();

		// perform test
		tested.doCleanup(mockRequest);

		verify();
		assertNotNull("Other should still be left", mockSession.getAttribute("Other"));
		assertNotNull("SomeFlowExecution should still be left", mockSession.getAttribute("SomeFlowExecution"));
	}

	public void testHasExpired() {
		int timeoutInMinutes = 1;
		int timeoutInMillis = 60000 * timeoutInMinutes;
		int overshootInMillis = 1000;
		long now = System.currentTimeMillis();
		long lastRequestTime = now - timeoutInMillis - overshootInMillis;

		tested.setTimeout(timeoutInMinutes);

		flowExecutionControl.expectAndReturn(flowExecutionMock.getLastRequestTimestamp(), lastRequestTime);
		replay();

		// perform test
		boolean result = tested.hasExpired(mockRequest, flowExecutionMock);

		verify();
		assertEquals("hasExpired,", true, result);
	}

	public void testHasNotExpired() {
		int timeoutInMinutes = 1;
		int timeoutInMillis = 60000 * timeoutInMinutes;
		int undershootInMillis = 1000;
		long now = System.currentTimeMillis();
		long lastRequestTime = now - timeoutInMillis + undershootInMillis;

		tested.setTimeout(timeoutInMinutes);

		flowExecutionControl.expectAndReturn(flowExecutionMock.getLastRequestTimestamp(), lastRequestTime);
		replay();

		// perform test
		boolean result = tested.hasExpired(mockRequest, flowExecutionMock);

		verify();
		assertEquals("hasExpired,", false, result);
	}
}