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
package org.springframework.webflow.manager.mvc;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.manager.FlowExecutionManager;

/**
 * Unit test for the FlowController class.
 * 
 * @author Ulrik Sandberg
 */
public class PortletFlowControllerTests extends TestCase {

	private MockControl flowExecutionManagerControl;

	private FlowExecutionManager flowExecutionManagerMock;

	private MockControl flowLocatorControl;

	private FlowLocator flowLocatorMock;

	private PortletFlowController tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionManagerControl = MockControl.createControl(FlowExecutionManager.class);
		flowExecutionManagerMock = (FlowExecutionManager) flowExecutionManagerControl.getMock();

		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator) flowLocatorControl.getMock();

		tested = new PortletFlowController(flowExecutionManagerMock);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flowExecutionManagerControl = null;
		flowExecutionManagerMock = null;
		flowLocatorControl = null;
		flowLocatorMock = null;
		tested = null;
	}

	protected void replay() {
		flowExecutionManagerControl.replay();
		flowLocatorControl.replay();
	}

	protected void verify() {
		flowExecutionManagerControl.verify();
		flowLocatorControl.verify();
	}

	public void testInitDefaultsFromFlowExecutionManagerConstructor() throws Exception {
		replay();
		int result = tested.getCacheSeconds();
		verify();
		assertEquals("Cache,", 0, result);
	}

	public void testInitDefaultsFromFlowLocatorConstructor() throws Exception {
		tested = new PortletFlowController(flowLocatorMock);
		replay();
		int result = tested.getCacheSeconds();
		verify();
		assertEquals("Cache,", 0, result);
	}

	public void testHandleRenderRequestInternal() throws Exception {
		final ViewSelection viewSelection = new ViewSelection("Some view");
		MockPortletSession mockPortletSession = new MockPortletSession();
		String attributeKey = PortletFlowController.class + ".viewSelection";
		mockPortletSession.setAttribute(attributeKey, viewSelection);

		MockRenderRequest mockRenderRequest = new MockRenderRequest();
		mockRenderRequest.setSession(mockPortletSession);
		replay();
		
		// perform test
		ModelAndView result = tested.handleRenderRequestInternal(mockRenderRequest, null);

		verify();
		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
		assertNull("Should have cleaned session", mockPortletSession.getAttribute(attributeKey));
	}

	public void testToModelAndViewWithView() {
		ViewSelection viewSelection = new ViewSelection("Some view");
		replay();

		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		verify();
		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
	}

	public void testToModelAndViewWithViewAndModel() {
		ViewSelection viewSelection = new ViewSelection("Some view", "Some name", "Some value");
		replay();
		
		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		verify();
		assertNotNull("Null result unexpected", result);
		assertEquals("Some view", result.getViewName());
		assertTrue("Model does not contain expected value", result.getModel().containsKey("Some name"));
	}

	public void testToModelAndViewWithRedirectView() {
		ViewSelection viewSelection = new ViewSelection("Some view");
		viewSelection.setRedirect(true);
		replay();

		// perform test
		ModelAndView result = tested.toModelAndView(viewSelection);

		verify();
		assertNotNull("Null result unexpected", result);
		assertEquals("redirect:Some view", result.getViewName());
	}

	public void testToModelAndViewWithNull() {
		replay();

		// perform test
		ModelAndView result = tested.toModelAndView(null);
		
		verify();
		assertNull("Null view should give null result", result);
	}
}