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
package org.springframework.webflow.executor.jsf;

import java.util.HashMap;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.jsf.FlowExecutionHolder;
import org.springframework.webflow.executor.jsf.FlowPhaseListener;

/**
 * Unit tests for the FlowPhaseListener class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowPhaseListenerTests extends TestCase {

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockControl flowExecutionRepositoryControl;

	private FlowExecutionRepository flowExecutionRepositoryMock;

	private MockFacesContext mockFacesContext;

	private MockJsfExternalContext mockJsfExternalContext;

	private MyLifecycle lifecycle;

	private FlowExecutionHolder flowExecutionHolder;

	private FlowExecutionContinuationKey flowExecutionContinuationKey;

	private FlowPhaseListener tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();

		flowExecutionRepositoryControl = MockControl.createControl(FlowExecutionRepository.class);
		flowExecutionRepositoryMock = (FlowExecutionRepository)flowExecutionRepositoryControl.getMock();

		mockFacesContext = new MockFacesContext();
		mockJsfExternalContext = new MockJsfExternalContext();
		mockFacesContext.setExternalContext(mockJsfExternalContext);
		lifecycle = new MyLifecycle();

		flowExecutionHolder = new FlowExecutionHolder(flowExecutionMock);
		flowExecutionContinuationKey = new FlowExecutionContinuationKey("some conversation id", "some continuation id");
		tested = new FlowPhaseListener() {
			protected FlowExecutionRepository getRepository(ExternalContext context) {
				return flowExecutionRepositoryMock;
			}
		};
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flowExecutionControl = null;
		flowExecutionMock = null;

		flowExecutionRepositoryControl = null;
		flowExecutionRepositoryMock = null;

		flowExecutionHolder = null;

		mockFacesContext = null;
		mockJsfExternalContext = null;

		lifecycle = null;
		tested = null;
	}

	protected void replay() {
		flowExecutionControl.replay();
		flowExecutionRepositoryControl.replay();
	}

	protected void verify() {
		flowExecutionControl.verify();
		flowExecutionRepositoryControl.verify();
	}

	public void testBeforePhaseNullHolder() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		replay();

		// perform test
		tested.beforePhase(event);

		verify();
	}

	public void testBeforePhaseNonNullHolder() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		HashMap requestMap = new HashMap();
		requestMap.put(FlowExecutionHolder.class.getName(), flowExecutionHolder);
		mockJsfExternalContext.setRequestMap(requestMap);

		// preparing holder with a null key
		flowExecutionHolder.setContinuationKey(null);

		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionRepositoryControl.expectAndReturn(flowExecutionRepositoryMock
				.generateContinuationKey(flowExecutionMock), flowExecutionContinuationKey);
		replay();

		// perform test
		tested.beforePhase(event);

		verify();
		assertEquals(flowExecutionContinuationKey, flowExecutionHolder.getContinuationKey());
		assertEquals("_ssome conversation id_csome continuation id", requestMap
				.get(FlowPhaseListener.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertEquals(flowExecutionMock, requestMap.get(FlowPhaseListener.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
	}

	public void testBeforePhaseNonNullHolderNonNullKey() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		HashMap requestMap = new HashMap();
		requestMap.put(FlowExecutionHolder.class.getName(), flowExecutionHolder);
		mockJsfExternalContext.setRequestMap(requestMap);

		// preparing holder with a key
		flowExecutionHolder
				.setContinuationKey(new FlowExecutionContinuationKey("some conversation id", "should change"));

		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionRepositoryControl.expectAndReturn(flowExecutionRepositoryMock.generateContinuationKey(
				flowExecutionMock, "some conversation id"), flowExecutionContinuationKey);
		replay();

		// perform test
		tested.beforePhase(event);

		verify();
		assertEquals(flowExecutionContinuationKey, flowExecutionHolder.getContinuationKey());
		assertEquals("_ssome conversation id_csome continuation id", requestMap
				.get(FlowPhaseListener.FLOW_EXECUTION_ID_ATTRIBUTE));
		assertEquals(flowExecutionMock, requestMap.get(FlowPhaseListener.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
	}

	public void testAfterPhaseNullEvent() {
		replay();
		try {
			tested.afterPhase(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException expected) {
			// expected
		}
		verify();
	}

	public void testAfterPhaseNotRestoreNotRender() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.ANY_PHASE, lifecycle);
		replay();

		// perform test
		tested.afterPhase(event);

		verify();
	}

	public void testAfterPhaseRestoreView() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RESTORE_VIEW, lifecycle);
		replay();

		// perform test
		tested.afterPhase(event);

		verify();
	}

	public void testAfterPhaseRestoreViewFlowExecutionIdHasText() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RESTORE_VIEW, lifecycle);
		HashMap requestParameterMap = new HashMap();
		requestParameterMap.put("_flowExecutionId", "_ssome conversation id_csome continuation id");
		mockJsfExternalContext.setRequestParameterMap(requestParameterMap);
		flowExecutionRepositoryControl.expectAndReturn(flowExecutionRepositoryMock
				.getFlowExecution(flowExecutionContinuationKey), flowExecutionMock);
		replay();

		// perform test
		tested.afterPhase(event);

		verify();
	}

	public void testAfterPhaseRenderResponse() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		replay();

		// perform test
		tested.afterPhase(event);

		verify();
	}

	public void testAfterPhaseRenderResponseNonNullHolder() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		HashMap requestMap = new HashMap();
		requestMap.put(FlowExecutionHolder.class.getName(), flowExecutionHolder);
		mockJsfExternalContext.setRequestMap(requestMap);

		// preparing holder with a key
		flowExecutionHolder.setContinuationKey(flowExecutionContinuationKey);

		flowExecutionControl.expectAndReturn(flowExecutionMock.isActive(), true);
		flowExecutionRepositoryMock.putFlowExecution(flowExecutionContinuationKey, flowExecutionMock);
		replay();

		// perform test
		tested.afterPhase(event);

		verify();
	}

	private static class MyLifecycle extends Lifecycle {
		public void addPhaseListener(PhaseListener listener) {
		}

		public void execute(FacesContext context) throws FacesException {
		}

		public PhaseListener[] getPhaseListeners() {
			return null;
		}

		public void removePhaseListener(PhaseListener listener) {
		}

		public void render(FacesContext context) throws FacesException {
		}
	}
}