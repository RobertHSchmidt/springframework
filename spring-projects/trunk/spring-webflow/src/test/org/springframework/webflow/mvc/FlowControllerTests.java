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
package org.springframework.webflow.mvc;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowLocator;
import org.springframework.webflow.State;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * Unit test for the FlowController class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowControllerTests extends TestCase {
	MockControl flowLocatorControl;

	FlowLocator flowLocatorMock;

	private FlowController tested;

	private MockHttpServletRequest mockRequest;

	protected void setUp() throws Exception {
		super.setUp();
		mockRequest = new MockHttpServletRequest();
		mockRequest.addParameter("_flowId", "SomeFlow");
		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator)flowLocatorControl.getMock();
		tested = new FlowController(flowLocatorMock);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockRequest = null;
		flowLocatorControl = null;
		flowLocatorMock = null;
		tested = null;
	}

	public void testInitDefaults() {
		assertEquals("cacheSeconds", 0, tested.getCacheSeconds());
	}

	public void testInitDefaultsNullFlowLocator() {
		FlowController localTested = new FlowController((FlowLocator)null);
		assertEquals("cacheSeconds", 0, localTested.getCacheSeconds());
	}

	public void testInitDefaultsNullFlowExecutionManager() {
		try {
			new FlowController((FlowExecutionManager)null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			// expected
		}
	}

	public void testHandleRequestInternal() throws Exception {
		Flow flow = new Flow("SomeFlow");
		final ViewSelection viewSelection = new ViewSelection("SomeView");
		flow.setStartState(new State(flow, "SomeState") {

			protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
				return viewSelection;
			}
		});
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("SomeFlow"), flow);
		flowLocatorControl.replay();

		// perform test
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);

		flowLocatorControl.verify();
		assertEquals("SomeView", result.getViewName());
	}

	public void testHandleRequestInternalWithNullView() throws Exception {
		Flow flow = new Flow("SomeFlow");
		flow.setStartState(new State(flow, "SomeState") {

			protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
				return null;
			}
		});
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("SomeFlow"), flow);
		flowLocatorControl.replay();

		// perform test
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);

		flowLocatorControl.verify();
		assertNull("view should be null", result);
	}

	public void testHandleRequestInternalWithRedirectView() throws Exception {
		Flow flow = new Flow("SomeFlow");
		final ViewSelection viewSelection = new ViewSelection("SomeView");
		viewSelection.setRedirect(true);
		flow.setStartState(new State(flow, "SomeState") {

			protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
				return viewSelection;
			}
		});
		flowLocatorControl.expectAndReturn(flowLocatorMock.getFlow("SomeFlow"), flow);
		flowLocatorControl.replay();

		// perform test
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);

		flowLocatorControl.verify();
		assertEquals("redirect:SomeView", result.getViewName());
	}
}