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

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.executor.jsf.FlowExecutionHolder;
import org.springframework.webflow.executor.jsf.FlowNavigationHandler;
import org.springframework.webflow.executor.jsf.JsfExternalContext;

/**
 * Unit tests for the FlowNavigationHandler class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowNavigationHandlerTests extends TestCase {

	private MockControl flowExecutionControl;

	private FlowExecution flowExecutionMock;

	private MockControl flowLocatorControl;

	private FlowLocator flowLocatorMock;

	private MockFacesContext mockFacesContext;

	private MockJsfExternalContext mockJsfExternalContext;

	private MyNavigationHandler navigationHandler;

	private FlowExecutionHolder flowExecutionHolder;

	private boolean renderViewCalled;

	private FlowNavigationHandler tested;

	private ViewSelection viewSelection;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionControl = MockControl.createControl(FlowExecution.class);
		flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();
		flowExecutionControl.setDefaultMatcher(new JsfExternalContextMatcher());

		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator)flowLocatorControl.getMock();

		viewSelection = new ViewSelection("SomeView", null, false);
		navigationHandler = new MyNavigationHandler();
		mockFacesContext = new MockFacesContext();
		mockJsfExternalContext = new MockJsfExternalContext();
		mockFacesContext.setExternalContext(mockJsfExternalContext);
		flowExecutionHolder = new FlowExecutionHolder(flowExecutionMock);
		tested = new FlowNavigationHandler(navigationHandler) {
			public void renderView(ViewSelection selectedView, FacesContext facesContext) {
				renderViewCalled = true;
			}

			protected FlowLocator getFlowLocator(FacesContext context) {
				return flowLocatorMock;
			}

			protected FlowExecution createFlowExecution(Flow flow, FacesContext context) {
				return flowExecutionMock;
			}
		};
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flowExecutionControl = null;
		flowExecutionMock = null;

		flowLocatorControl = null;
		flowLocatorMock = null;

		tested = null;
	}

	protected void replay() {
		flowExecutionControl.replay();
		flowLocatorControl.replay();
	}

	protected void verify() {
		flowExecutionControl.verify();
		flowLocatorControl.verify();
	}

	public void testHandleNavigationNoHolderNoFlowId() {
		replay();

		// perform test
		tested.handleNavigation(mockFacesContext, "FromAction", "OutCome");

		verify();
		assertTrue("delegate not called", navigationHandler.handled);
	}

	public void testHandleNavigationHasHolder() {
		mockJsfExternalContext.getRequestMap().put(FlowExecutionHolder.class.getName(), flowExecutionHolder);
		flowExecutionControl.expectAndReturn(flowExecutionMock.signalEvent("OutCome", new JsfExternalContext(
				mockFacesContext, "FromAction", "OutCome")), viewSelection);
		replay();

		// perform test
		tested.handleNavigation(mockFacesContext, "FromAction", "OutCome");

		verify();
		assertTrue("renderView not called", renderViewCalled);
		assertFalse("delegate called", navigationHandler.handled);
	}

	public void testHandleNavigationHasNoHolder() {
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("OutCome"), null);
		flowExecutionControl.expectAndReturn(flowExecutionMock.start(new JsfExternalContext(mockFacesContext,
				"FromAction", "flowId:OutCome")), viewSelection);
		replay();

		// perform test
		tested.handleNavigation(mockFacesContext, "FromAction", "flowId:OutCome");

		verify();
		assertFalse("delegate called", navigationHandler.handled);
	}

	private static class MyNavigationHandler extends NavigationHandler {
		boolean handled;

		public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
			handled = true;
		}
	}
}