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
package org.springframework.webflow.portlet;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.FlowLocator;

/**
 * Unit test for the FlowController class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowControllerTests extends TestCase {

	private MockControl flowLocatorControl;

	private FlowLocator flowLocatorMock;

	private FlowController tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator)flowLocatorControl.getMock();
		tested = new FlowController(flowLocatorMock);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flowLocatorControl = null;
		flowLocatorMock = null;
		tested = null;
	}

	public void testInit() throws Exception {
		FlowExecutionManager flowExecutionManager = new FlowExecutionManager(flowLocatorMock);
		FlowController localTested = new FlowController(flowExecutionManager);
		assertEquals("Cache,", 0, localTested.getCacheSeconds());
	}

	public void testHandleActionRequestInternal() throws Exception {
		final ViewSelection viewSelection = new ViewSelection("Some view");
		MockPortletSession mockPortletSession = new MockPortletSession();
		String attributeKey = FlowController.class + ".viewSelection";
		mockPortletSession.setAttribute(attributeKey, viewSelection);

		MockActionRequest mockActionRequest = new MockActionRequest();
		mockActionRequest.setSession(mockPortletSession);

		FlowExecutionManager flowExecutionManager = new FlowExecutionManager(flowLocatorMock) {
			public ViewSelection onEvent(ExternalContext context) {
				return viewSelection;
			}
		};
		tested.setFlowExecutionManager(flowExecutionManager);

		// perform test
		tested.handleActionRequestInternal(mockActionRequest, null);

		assertNotNull("No view in session", mockPortletSession.getAttribute(attributeKey));
	}

	public void testHandleRenderRequestInternal() throws Exception {
		final ViewSelection viewSelection = new ViewSelection("Some view");
		MockPortletSession mockPortletSession = new MockPortletSession();
		String attributeKey = FlowController.class + ".viewSelection";
		mockPortletSession.setAttribute(attributeKey, viewSelection);

		MockRenderRequest mockRenderRequest = new MockRenderRequest();
		mockRenderRequest.setSession(mockPortletSession);

		FlowExecutionManager flowExecutionManager = new FlowExecutionManager(flowLocatorMock) {
			public ViewSelection onEvent(ExternalContext context) {
				return viewSelection;
			}
		};
		tested.setFlowExecutionManager(flowExecutionManager);

		// perform test
		ModelAndView result = tested.handleRenderRequestInternal(mockRenderRequest, null);

		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
		assertNull("Should have cleaned session", mockPortletSession.getAttribute(attributeKey));
	}

	public void testHandleRenderRequestInternalNullSelectedView() throws Exception {
		final ViewSelection viewSelection = new ViewSelection("Some view");
		MockPortletSession mockPortletSession = new MockPortletSession();

		MockRenderRequest mockRenderRequest = new MockRenderRequest();
		mockRenderRequest.setSession(mockPortletSession);

		FlowExecutionManager flowExecutionManager = new FlowExecutionManager(flowLocatorMock) {
			public ViewSelection onEvent(ExternalContext context) {
				return viewSelection;
			}
		};
		tested.setFlowExecutionManager(flowExecutionManager);

		// perform test
		ModelAndView result = tested.handleRenderRequestInternal(mockRenderRequest, null);

		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
	}

	public void testToModelAndViewWithView() {
		ViewSelection viewSelection = new ViewSelection("Some view");

		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
	}

	public void testToModelAndViewWithViewAndModel() {
		ViewSelection viewSelection = new ViewSelection("Some view", "Some name", "Some value");

		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
		assertTrue("Model does not contain expected value", result.getModel().containsKey("Some name"));
	}

	public void testToModelAndViewWithRedirectView() {
		ViewSelection viewSelection = new ViewSelection("Some view");
		viewSelection.setRedirect(true);

		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		assertNotNull("Null result unexpected", result);
		assertEquals("redirect:Some view", result.getViewName());
	}

	public void testToModelAndViewWithNull() {

		// perform test
		ModelAndView result = tested.toModelAndView(null);

		assertNull("Null view should give null result", result);
	}
}
