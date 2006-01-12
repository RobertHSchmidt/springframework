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
package org.springframework.webflow.manager.mvc;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.manager.FlowExecutionManager;

/**
 * Unit test for the FlowController class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowControllerTests extends TestCase {
	private MockControl flowExecutionManagerControl;

	private FlowExecutionManager flowExecutionManagerMock;

	private FlowController tested;

	private MockHttpServletRequest mockRequest;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionManagerControl = MockControl.createControl(FlowExecutionManager.class);
		flowExecutionManagerMock = (FlowExecutionManager)flowExecutionManagerControl.getMock();
		tested = new FlowController(flowExecutionManagerMock);	}

	public void testInitDefaults() {
		assertEquals("cacheSeconds", 0, tested.getCacheSeconds());
	}

	public void testInitDefaultsNullFlowLocator() {
		assertEquals("cacheSeconds", 0, tested.getCacheSeconds());
	}

	public void testHandleRequestInternal() throws Exception {
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);
		assertEquals("SomeView", result.getViewName());
	}

	public void testHandleRequestInternalWithNullView() throws Exception {
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);
		assertNull("view should be null", result);
	}

	public void testHandleRequestInternalWithRedirectView() throws Exception {
		final ViewSelection viewSelection = new ViewSelection("SomeView");
		viewSelection.setRedirect(true);
		ModelAndView result = tested.handleRequestInternal(mockRequest, null);
		assertEquals("redirect:SomeView", result.getViewName());
	}
}