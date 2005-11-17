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
package org.springframework.webflow.jsf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.config.FlowLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * Unit test for the JsfFlowExecutionManager class.
 * 
 * @author Ulrik Sandberg
 */
public class JsfFlowExecutionManagerTests extends TestCase {
	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockFacesContext mockFacesContext;

	private FlowLocator flowLocator;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();

		MockExternalContext mockExternalContext = new MockExternalContext();
		// HashMap requestMap = new HashMap();
		// requestMap.put("SomeKey", "SomeValue");
		// mockExternalContext.setRequestMap(requestMap);

		mockFacesContext = new MockFacesContext();
		mockFacesContext.setExternalContext(mockExternalContext);
		flowLocator = new FlowLocator() {
			public Flow getFlow(String id) throws FlowArtifactException {
				Flow flow = new Flow("SomeFlow");
				flow.setStartState(new State(flow, "SomeState") {
					protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
						return null;
					}
				});
				return flow;
			}
		};
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockFacesContext = null;
		flowLocator = null;
	}

	public void testIsFlowLaunchRequest() {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		boolean result = tested.isFlowLaunchRequest(mockFacesContext, null, JsfFlowExecutionManager.FLOW_ID_PREFIX
				+ "SomeOutcome");

		assertEquals(true, result);
	}

	public void testIsFlowLaunchRequestNoPrefix() {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		boolean result = tested.isFlowLaunchRequest(mockFacesContext, null, "SomeOutcome");

		assertEquals(false, result);
	}

	public void testIsFlowLaunchRequestNullOutcome() {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		boolean result = tested.isFlowLaunchRequest(mockFacesContext, null, null);

		assertEquals(false, result);
	}

	/*
	 * This test only verifies that the expected methods and delegates are
	 * called. The methods are tested elsewhere in this class.
	 */
	public void testLaunchFlowExecution() throws Exception {
		final JsfEvent startEvent = new JsfEvent("SomeOutcome", mockFacesContext, "FromAction", null);
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			// not interested in testing this method in this test
			protected ViewSelection prepareSelectedView(ViewSelection selectedView, Serializable flowExecutionId,
					FlowExecutionContext flowExecutionContext) {
				return selectedView;
			}

			// not interested in testing this method in this test
			public JsfEvent createEvent(FacesContext context, String fromAction, String outcome, Map parameters) {
				return startEvent;
			}

			// not interested in testing this method in this test
			protected Serializable manageStorage(Serializable flowExecutionId, FlowExecution flowExecution,
					Event sourceEvent) {
				return new Serializable() {
				};
			}

			// not interested in testing this method in this test
			protected FlowExecution createFlowExecution(Flow flow) {
				return flowExecutionMock;
			}
		};

		flowExecutionControl.expectAndReturn(flowExecutionMock.start(startEvent), new ViewSelection("SomeView"));
		flowExecutionControl.replay();

		// perform test
		ViewSelection viewSelection = tested.launchFlowExecution(mockFacesContext, "FromAction", "SomeOutcome");

		flowExecutionControl.verify();
		assertEquals("SomeView", viewSelection.getViewName());
	}

	public void testIsFlowExecutionParticipationRequest() throws Exception {
		MockExternalContext mockExternalContext = new MockExternalContext();
		HashMap requestParameterMap = new HashMap();
		Serializable flowExecutionId = new Serializable() {
		};
		requestParameterMap.put(FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER, flowExecutionId);
		mockExternalContext.setRequestParameterMap(requestParameterMap);
		mockFacesContext.setExternalContext(mockExternalContext);
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);

		// prepare the holder with an id and a flow execution
		FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecutionMock, null, false);

		flowExecutionControl.replay();

		// perform test
		boolean result = tested.isFlowExecutionParticipationRequest(mockFacesContext, "FromAction", "SomeOutcome");

		flowExecutionControl.verify();
		assertEquals(true, result);
	}

	public void testIsFlowExecutionParticipationRequestNoFlowExecution() throws Exception {
		MockExternalContext mockExternalContext = new MockExternalContext();
		HashMap requestParameterMap = new HashMap();
		mockExternalContext.setRequestParameterMap(requestParameterMap);
		mockFacesContext.setExternalContext(mockExternalContext);
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);

		// prepare the holder with nothing
		FlowExecutionHolder.setFlowExecution(null, null, null, false);

		flowExecutionControl.replay();

		// perform test
		boolean result = tested.isFlowExecutionParticipationRequest(mockFacesContext, "FromAction", "SomeOutcome");

		flowExecutionControl.verify();
		assertEquals(false, result);
	}
}