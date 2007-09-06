/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.impl;

import junit.framework.TestCase;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.MockFlowExecutionListener;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;

/**
 * General flow execution tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Ben Hale
 */
public class FlowExecutionImplTests extends TestCase {

	public void testStartAndEnd() {
		Flow flow = new Flow("flow");
		EndState state = new EndState(flow, "end");
		MockFlowExecutionListener mockListener = new MockFlowExecutionListener();
		FlowExecutionListener[] listeners = new FlowExecutionListener[] { mockListener };
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, listeners, null, null, null);
		MockExternalContext context = new MockExternalContext();
		assertFalse(execution.hasStarted());
		execution.start(null, context);
		assertTrue(execution.hasStarted());
		assertFalse(execution.isActive());
		assertEquals(1, mockListener.getRequestsSubmittedCount());
		assertEquals(1, mockListener.getRequestsProcessedCount());
		assertEquals(1, mockListener.getSessionCreatingCount());
		assertEquals(1, mockListener.getSessionStartingCount());
		assertEquals(1, mockListener.getSessionStartedCount());
		assertEquals(1, mockListener.getStateEnteringCount());
		assertEquals(1, mockListener.getStateEnteredCount());
		assertEquals(1, mockListener.getSessionEndingCount());
		assertEquals(1, mockListener.getSessionEndedCount());
		assertEquals(0, mockListener.getEventSignaledCount());
		assertEquals(0, mockListener.getPausedCount());
		assertEquals(0, mockListener.getResumedCount());
		assertEquals(0, mockListener.getExceptionThrownCount());
		assertEquals(0, mockListener.getFlowNestingLevel());
	}

	public void testStartExceptionThrownBeforeFirstSessionCreated() {
		Flow flow = new Flow("flow");
		EndState state = new EndState(flow, "end");
		FlowExecutionListener mockListener = new FlowExecutionListenerAdapter() {
			public void sessionCreating(RequestContext context, FlowDefinition definition, MutableAttributeMap input) {
				throw new IllegalStateException("Oops");
			}
		};
		FlowExecutionListener[] listeners = new FlowExecutionListener[] { mockListener };
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, listeners, null, null, null);
		MockExternalContext context = new MockExternalContext();
		assertFalse(execution.hasStarted());
		try {
			execution.start(null, context);
		} catch (FlowExecutionException e) {
			assertEquals(e.getFlowId(), "flow");
		}

	}

	public void testStartTwice() {
		Flow flow = new Flow("flow");
		EndState state = new EndState(flow, "end");
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, null, null, null, null);
		MockExternalContext context = new MockExternalContext();
		execution.start(null, context);
		try {
			execution.start(null, context);
			fail("Should've failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testResumeAfterEnding() {
		Flow flow = new Flow("flow");
		EndState state = new EndState(flow, "end");
		FlowExecutionImpl execution = new FlowExecutionImpl(flow, null, null, null, null);
		MockExternalContext context = new MockExternalContext();
		execution.start(null, context);
		try {
			execution.resume(context);
			fail("Should've failed");
		} catch (IllegalStateException e) {

		}
	}

}