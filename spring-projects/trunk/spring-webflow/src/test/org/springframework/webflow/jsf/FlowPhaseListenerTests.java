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
package org.springframework.webflow.jsf;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.springframework.webflow.execution.FlowLocator;

/**
 * Unit test for the FlowPhaseListener class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowPhaseListenerTests extends TestCase {

	private FlowPhaseListener tested;

	private MyFlowExecutionManager executionManager;

	private MockFacesContext mockFacesContext;

	private MyLifecycle lifecycle;

	protected void setUp() throws Exception {
		super.setUp();
		mockFacesContext = new MockFacesContext();
		lifecycle = new MyLifecycle();
		executionManager = new MyFlowExecutionManager(null);
		tested = new FlowPhaseListener() {
			protected JsfFlowExecutionManager getExecutionManager(FacesContext context) {
				return executionManager;
			}
		};
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockFacesContext = null;
		lifecycle = null;
		executionManager = null;
		tested = null;
	}

	public void testBeforePhase() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		tested.beforePhase(event);
	}

	public void testAfterPhaseNullEvent() {
		try {
			tested.afterPhase(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException expected) {
			// expected
		}
	}

	public void testAfterPhaseNotRestoreNotRender() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.ANY_PHASE, lifecycle);
		tested.afterPhase(event);
	}

	public void testAfterPhaseRestoreView() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RESTORE_VIEW, lifecycle);

		// perform test
		tested.afterPhase(event);

		assertTrue("restore not called", executionManager.restored);
	}

	public void testAfterPhaseRenderResponse() {
		PhaseEvent event = new PhaseEvent(mockFacesContext, PhaseId.RENDER_RESPONSE, lifecycle);
		// perform test
		tested.afterPhase(event);
		assertTrue("save not called", executionManager.saved);
	}

	private static class MyFlowExecutionManager extends JsfFlowExecutionManager {
		boolean restored;

		boolean saved;

		private MyFlowExecutionManager(FlowLocator locator) {
			super(locator);
		}

		public void saveFlowExecutionIfNecessary(FacesContext context) {
			saved = true;
		}

		public void restoreFlowExecution(FacesContext context) {
			restored = true;
		}
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