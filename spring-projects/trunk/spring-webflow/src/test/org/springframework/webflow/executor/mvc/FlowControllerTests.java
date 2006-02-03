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
package org.springframework.webflow.executor.mvc;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.mvc.FlowController;

/**
 * Unit test for the FlowController class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowControllerTests extends TestCase {
	private MockControl flowExecutionManagerControl;

	private FlowExecutor flowExecutionManagerMock;

	private MockControl flowLocatorControl;

	private FlowLocator flowLocatorMock;

	private FlowController tested;

	protected void setUp() throws Exception {
		super.setUp();
		flowExecutionManagerControl = MockControl.createControl(FlowExecutor.class);
		flowExecutionManagerMock = (FlowExecutor) flowExecutionManagerControl.getMock();

		flowLocatorControl = MockControl.createControl(FlowLocator.class);
		flowLocatorMock = (FlowLocator) flowLocatorControl.getMock();

		tested = new FlowController(flowExecutionManagerMock);
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

	public void testInitDefaultsFromFlowExecutionManagerConstructor() {
		replay();
		int result = tested.getCacheSeconds();
		verify();
		assertEquals("cacheSeconds", 0, result);
	}

	public void testInitDefaultsFromFlowLocatorConstructor() {
		tested = new FlowController(flowLocatorMock);
		replay();
		int result = tested.getCacheSeconds();
		verify();
		assertEquals("cacheSeconds", 0, result);
	}

	public void testToModelAndView() throws Exception {
		replay();
		ModelAndView result = tested.toModelAndView(new ViewSelection("SomeView", null, false));
		verify();
		assertEquals("SomeView", result.getViewName());
	}

	public void testToModelAndViewWithRedirectView() throws Exception {
		ViewSelection viewSelection = new ViewSelection("SomeView", null, true);
		replay();
		ModelAndView result = tested.toModelAndView(viewSelection);
		verify();
		assertEquals("redirect:SomeView", result.getViewName());
	}
}