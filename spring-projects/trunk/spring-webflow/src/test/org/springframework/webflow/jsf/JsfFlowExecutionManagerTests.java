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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListenerList;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.FlowExecutionStorage;
import org.springframework.webflow.execution.FlowExecutionStorageException;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.test.MockExternalContext;

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

	private MockJsfExternalContext mockExternalContext;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();
		mockExternalContext = new MockJsfExternalContext();
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
		mockExternalContext = null;
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
		final JsfExternalContext jsfContext = new JsfExternalContext(mockFacesContext, "FromAction", "SomeOutcome");
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			// not interested in testing this method in this test
			protected ViewSelection prepareSelectedView(ViewSelection selectedView, Serializable flowExecutionId,
					FlowExecutionContext flowExecutionContext) {
				return selectedView;
			}

			// not interested in testing this method in this test
			protected FlowExecution createFlowExecution(Flow flow) {
				return flowExecutionMock;
			}

			protected ExternalContext createExternalContext(FacesContext context, String fromAction, String outcome) {
				return jsfContext;
			}
		};

		flowExecutionControl.expectAndReturn(flowExecutionMock.start(jsfContext), new ViewSelection("SomeView"));
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionControl.replay();

		// perform test
		ViewSelection viewSelection = tested.launchFlowExecution(mockFacesContext, "FromAction", "SomeOutcome");

		flowExecutionControl.verify();
		assertEquals("SomeView", viewSelection.getViewName());
	}

	public void testIsFlowExecutionParticipationRequest() throws Exception {
		MockJsfExternalContext mockExternalContext = new MockJsfExternalContext();
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
		MockJsfExternalContext mockExternalContext = new MockJsfExternalContext();
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

	public void testManageStorageFlowNotActiveNullId() throws Exception {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), false);
		flowExecutionControl.replay();

		// perform test
		Serializable flowExecutionId = tested.manageStorage(null, flowExecutionMock, null);

		flowExecutionControl.verify();
		assertNull("should be null id", flowExecutionId);
	}

	public void testManageStorageFlowNotActive() throws Exception {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			protected void removeFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution,
					ExternalContext context) throws FlowExecutionStorageException {
			}
		};

		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), false);
		flowExecutionControl.replay();

		// perform test
		Serializable flowExecutionId = tested.manageStorage(new Serializable() {
		}, flowExecutionMock, null);

		flowExecutionControl.verify();
		assertNull("should be null id", flowExecutionId);
	}

	public void testManageStorageFlowActiveSupportsTwoPhase() throws Exception {
		MockControl flowExecutionStorageControl = MockControl.createControl(FlowExecutionStorage.class);
		final FlowExecutionStorage flowExecutionStorageMock = (FlowExecutionStorage)flowExecutionStorageControl
				.getMock();

		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {

			protected FlowExecutionStorage getStorage() {
				return flowExecutionStorageMock;
			}
		};

		MockExternalContext context = new MockExternalContext();
		flowExecutionStorageControl.expectAndReturn(flowExecutionStorageMock.supportsTwoPhaseSave(), true);
		flowExecutionStorageControl.expectAndReturn(flowExecutionStorageMock.generateId(null, context), new Serializable() {
		});

		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);

		flowExecutionStorageControl.replay();
		flowExecutionControl.replay();

		// perform test
		Serializable flowExecutionId = tested.manageStorage(new Serializable() {
		}, flowExecutionMock, context);

		flowExecutionStorageControl.verify();
		flowExecutionControl.verify();
		assertNotNull("should not be null id", flowExecutionId);
	}

	public void testManageStorageFlowActiveDoesNotSupportTwoPhase() throws Exception {
		MockControl flowExecutionStorageControl = MockControl.createControl(FlowExecutionStorage.class);
		final FlowExecutionStorage flowExecutionStorageMock = (FlowExecutionStorage)flowExecutionStorageControl
				.getMock();

		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			protected FlowExecutionStorage getStorage() {
				return flowExecutionStorageMock;
			}

			public Serializable saveFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution,
					ExternalContext context) throws FlowExecutionStorageException {
				return flowExecutionId;
			}
		};
		flowExecutionStorageControl.expectAndReturn(flowExecutionStorageMock.supportsTwoPhaseSave(), false);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionStorageControl.replay();
		flowExecutionControl.replay();

		// perform test
		Serializable flowExecutionId = tested.manageStorage(new Serializable() {
		}, flowExecutionMock, null);

		flowExecutionStorageControl.verify();
		flowExecutionControl.verify();
		assertNotNull("should not be null id", flowExecutionId);
	}

	public void testSaveFlowExecutionIfNecessary() throws Exception {
		MockControl flowExecutionStorageControl = MockControl.createControl(FlowExecutionStorage.class);
		final FlowExecutionStorage flowExecutionStorageMock = (FlowExecutionStorage)flowExecutionStorageControl
				.getMock();

		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			protected FlowExecutionStorage getStorage() {
				return flowExecutionStorageMock;
			}
		};

		Serializable expectedFlowExecutionId = new Serializable() {
		};
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		State state = new ViewState(new Flow("Some Flow"), "state");
		flowExecutionControl.expectAndReturn(flowExecutionMock.getCurrentState(), state);

		FlowExecutionHolder.setFlowExecution(expectedFlowExecutionId, flowExecutionMock, null, false);
		flowExecutionStorageMock.saveWithGeneratedId(expectedFlowExecutionId, flowExecutionMock, null);
		flowExecutionControl.expectAndReturn(flowExecutionMock.getListeners(), new FlowExecutionListenerList());
		flowExecutionStorageControl.replay();
		flowExecutionControl.replay();

		HashMap requestMap = new HashMap();
		mockExternalContext.setRequestMap(requestMap);

		// perform test
		tested.saveFlowExecutionIfNecessary(mockFacesContext);

		flowExecutionStorageControl.verify();
		flowExecutionControl.verify();
		assertTrue(FlowExecutionHolder.isFlowExecutionSaved());
		assertTrue(requestMap.containsKey(FlowExecutionManager.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
	}

	public void testRenderView() throws Exception {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		ViewSelection viewSelection = new ViewSelection("SomeView", "SomeKey", "SomeValue");
		HashMap requestMap = new HashMap();
		mockExternalContext.setRequestMap(requestMap);

		MockViewHandler mockViewHandler = new MockViewHandler();
		UIViewRoot viewRoot = new UIViewRoot();
		viewRoot.setId("SomeViewRootId");
		mockViewHandler.setCreateView(viewRoot);

		MockApplication mockApplication = new MockApplication();
		mockApplication.setViewHandler(mockViewHandler);
		mockFacesContext.setApplication(mockApplication);

		// perform test
		tested.renderView(mockFacesContext, "FromAction", "SomeOutcome", viewSelection);
		assertEquals("SomeViewRootId", mockFacesContext.getViewRoot().getId());
		assertEquals("SomeValue", mockExternalContext.getRequestMap().get("SomeKey"));
	}

	public void testRenderViewPutAllNotSupported() throws Exception {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator);
		ViewSelection viewSelection = new ViewSelection("SomeView", "SomeKey", "SomeValue");
		HashMap requestMap = new HashMap() {
			public void putAll(Map m) {
				throw new UnsupportedOperationException("Fake bug in MyFaces");
			}
		};
		mockExternalContext.setRequestMap(requestMap);

		MockViewHandler mockViewHandler = new MockViewHandler();
		UIViewRoot viewRoot = new UIViewRoot();
		viewRoot.setId("SomeViewRootId");
		mockViewHandler.setCreateView(viewRoot);

		MockApplication mockApplication = new MockApplication();
		mockApplication.setViewHandler(mockViewHandler);
		mockFacesContext.setApplication(mockApplication);

		// perform test
		tested.renderView(mockFacesContext, "FromAction", "SomeOutcome", viewSelection);
		assertEquals("SomeViewRootId", mockFacesContext.getViewRoot().getId());
		assertEquals("SomeValue", mockExternalContext.getRequestMap().get("SomeKey"));
	}

	public void testRestoreFlowExecution() throws Exception {
		JsfFlowExecutionManager tested = new JsfFlowExecutionManager(flowLocator) {
			// not interested in testing this method in this test
			public FlowExecution loadFlowExecution(Serializable flowExecutionId, ExternalContext context)
					throws FlowExecutionStorageException {
				return flowExecutionMock;
			}
		};

		flowExecutionControl.replay();

		HashMap requestParameterMap = new HashMap();
		Serializable flowExecutionId = new Serializable() {
		};
		requestParameterMap.put(FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER, flowExecutionId);
		MockJsfExternalContext mockExternalContext = (MockJsfExternalContext)mockFacesContext.getExternalContext();
		mockExternalContext.setRequestParameterMap(requestParameterMap);

		// perform test
		tested.restoreFlowExecution(mockFacesContext);
		flowExecutionControl.verify();
		assertSame(flowExecutionId, FlowExecutionHolder.getFlowExecutionId());
	}
}