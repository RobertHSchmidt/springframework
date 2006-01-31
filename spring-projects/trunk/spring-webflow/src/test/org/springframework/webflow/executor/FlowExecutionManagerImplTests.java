/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.executor;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * Unit tests for the FlowExecutionManagerImpl class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowExecutionManagerImplTests extends TestCase {
	private MockControl flowLocatorControl;

	private FlowLocator flowLocatorMock;

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockControl flowExecutionRepositoryControl;

	private FlowExecutionRepository flowExecutionRepositoryMock;

	private MockControl flowExecutionContextControl;

	private FlowExecutionContext flowExecutionContextMock;

	private ViewSelection viewSelection;

	private FlowExecutionContinuationKey flowExecutionContinuationKey;

	private FlowExecutorImpl tested;

	protected void setUp() throws Exception {
		super.setUp();

		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator) flowLocatorControl.getMock();

		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution) flowExecutionControl.getMock();

		flowExecutionRepositoryControl = MockControl.createControl(FlowExecutionRepository.class);
		flowExecutionRepositoryMock = (FlowExecutionRepository) flowExecutionRepositoryControl.getMock();

		flowExecutionContextControl = MockControl.createControl(FlowExecutionContext.class);
		flowExecutionContextMock = (FlowExecutionContext) flowExecutionContextControl.getMock();

		Map model = new HashMap(1);
		model.put("SomeKey", "SomeValue");
		viewSelection = new ViewSelection("SomeView", model, false);
		flowExecutionContinuationKey = new FlowExecutionContinuationKey("some conversation id", "some continuation id");

		// need to override a few methods that we don't want to test here
		tested = new FlowExecutorImpl(flowLocatorMock) {
			protected FlowExecution createFlowExecution(Flow flow) {
				return flowExecutionMock;
			}

			protected FlowExecutionRepository getRepository(ExternalContext context) {
				return flowExecutionRepositoryMock;
			}

			protected ViewSelection prepareSelectedView(ViewSelection selectedView, FlowExecutionRepository repository, FlowExecutionContinuationKey continuationKey,
					FlowExecutionContext flowExecutionContext) {
				return new ViewSelection("PreparedView", null, false);
			}

			protected FlowExecutionContinuationKey parseContinuationKey(String flowExecutionId) {
				return flowExecutionContinuationKey;
			}

			public FlowExecution loadFlowExecution(FlowExecutionRepository repository,
					FlowExecutionContinuationKey continuationKey) {
				return flowExecutionMock;
			}
		};
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flowLocatorControl = null;
		flowLocatorMock = null;

		flowExecutionControl = null;
		flowExecutionMock = null;

		flowExecutionRepositoryControl = null;
		flowExecutionRepositoryMock = null;

		flowExecutionContextControl = null;
		flowExecutionContextMock = null;

		viewSelection = null;
		tested = null;
	}

	protected void replay() {
		flowLocatorControl.replay();
		flowExecutionControl.replay();
		flowExecutionRepositoryControl.replay();
		flowExecutionContextControl.replay();
	}

	protected void verify() {
		flowLocatorControl.verify();
		flowExecutionControl.verify();
		flowExecutionRepositoryControl.verify();
		flowExecutionContextControl.verify();
	}

	public void testLaunchNonActiveFlowExecution() throws Exception {
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("SomeFlow"), null);
		flowExecutionControl.expectAndReturn(flowExecutionMock.start(null), viewSelection);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), false);
		replay();

		// perform test
		ViewSelection result = tested.launch("SomeFlow", null);

		verify();
		assertEquals("SomeView", result.getViewName());
	}

	public void testLaunchActiveFlowExecution() throws Exception {
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("SomeFlow"), null);
		flowExecutionControl.expectAndReturn(flowExecutionMock.start(null), viewSelection);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionRepositoryControl.expectAndReturn(flowExecutionRepositoryMock.generateContinuationKey(flowExecutionMock), flowExecutionContinuationKey);
		flowExecutionRepositoryMock.putFlowExecution(flowExecutionContinuationKey, flowExecutionMock);
		replay();

		// perform test
		ViewSelection result = tested.launch("SomeFlow", null);

		verify();
		assertEquals("PreparedView", result.getViewName());
	}

	public void testSignalEventNonActiveFlowExecution() throws Exception {
		flowExecutionControl.expectAndReturn(flowExecutionMock.signalEvent("SomeEvent", null), viewSelection);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), false);
		flowExecutionRepositoryMock.invalidateConversation("some conversation id");
		replay();

		// perform test
		ViewSelection result = tested.signalEvent("SomeEvent", "SomeExecutionId", null);

		verify();
		assertEquals("SomeView", result.getViewName());
	}

	public void testSignalEventActiveFlowExecution() throws Exception {
		flowExecutionControl.expectAndReturn(flowExecutionMock.signalEvent("SomeEvent", null), viewSelection);
		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionRepositoryControl.expectAndReturn(flowExecutionRepositoryMock.generateContinuationKey(flowExecutionMock,
				"some conversation id"), null);
		flowExecutionRepositoryMock.putFlowExecution(null, flowExecutionMock);
		replay();

		// perform test
		ViewSelection result = tested.signalEvent("SomeEvent", "SomeExecutionId", null);

		verify();
		assertEquals("PreparedView", result.getViewName());
	}

	public void testPrepareSelectedViewNonActiveFlowExecutionContext() throws Exception {
		flowExecutionContextControl.expectAndReturn(flowExecutionContextMock.isActive(), false);
		tested = new FlowExecutorImpl(flowLocatorMock);
		replay();

		// perform test
		ViewSelection result = tested.prepareSelectedView(viewSelection, null, flowExecutionContinuationKey, flowExecutionContextMock);

		verify();
		assertEquals("SomeView", result.getViewName());
	}

	public void testPrepareSelectedViewActiveFlowExecutionContext() throws Exception {
		flowExecutionContextControl.expectAndReturn(flowExecutionContextMock.isActive(), true);
		tested = new FlowExecutorImpl(flowLocatorMock) {
			protected String formatContinuationKey(FlowExecutionContinuationKey key) {
				return "FormattedKey";
			}
		};
		replay();

		// perform test
		ViewSelection result = tested.prepareSelectedView(viewSelection, null, flowExecutionContinuationKey, flowExecutionContextMock);

		verify();
		assertEquals("SomeView", result.getViewName());
		assertEquals("FormattedKey", result.getModel().get(FlowExecutorImpl.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertEquals(flowExecutionContextMock, result.getModel().get(FlowExecutorImpl.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
	}

	public void testPrepareSelectedViewRedirect() throws Exception {
		flowExecutionContextControl.expectAndReturn(flowExecutionContextMock.isActive(), true);
		tested = new FlowExecutorImpl(flowLocatorMock);
		ViewSelection viewSelection = new ViewSelection("Some Prepared View", null, true);
		replay();

		// perform test
		MockControl control = MockControl.createControl(FlowExecutionRepository.class);
		FlowExecutionRepository repositoryMock = (FlowExecutionRepository)control.getMock();
		repositoryMock.setCurrentViewSelection(flowExecutionContinuationKey.getConversationId(), viewSelection);
		control.replay();
		
		tested.prepareSelectedView(viewSelection, repositoryMock, flowExecutionContinuationKey, flowExecutionContextMock);

		verify();
		control.verify();
	}
}