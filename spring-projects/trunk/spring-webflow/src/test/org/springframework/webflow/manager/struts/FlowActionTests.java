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
package org.springframework.webflow.manager.struts;

import junit.framework.TestCase;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.ViewSelection;

/**
 * Unit tests for the FlowAction class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowActionTests extends TestCase {
	private MockHttpServletRequest mockRequest;

	private ViewSelection viewSelection;

	private ActionMapping nullForwardActionMapping;

	private ActionMapping validForwardActionMapping;

	private FlowAction tested;

	protected void setUp() throws Exception {
		super.setUp();

		mockRequest = new MockHttpServletRequest();

		viewSelection = new ViewSelection("SomeView", "SomeKey", "SomeValue");
		validForwardActionMapping = new ActionMapping() {
			public ActionForward findForward(String name) {
				return new ActionForward("SomeForward", "/somepath", false);
			}
		};
		nullForwardActionMapping = new ActionMapping() {
			public ActionForward findForward(String name) {
				return null;
			}
		};
		tested = new FlowAction();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockRequest = null;
		viewSelection = null;
		validForwardActionMapping = null;
		nullForwardActionMapping = null;
		tested = null;
	}

	public void testToActionForwardNullViewSelection() throws Exception {
		// perform test
		ActionForward forward = tested.toActionForward(null, null, null);

		assertNull(forward);
	}

	public void testToActionForwardValidViewSelectionValidForward() throws Exception {

		// perform test
		ActionForward forward = tested.toActionForward(viewSelection, validForwardActionMapping, mockRequest);

		assertEquals("Forward name,", "SomeForward", forward.getName());
		assertEquals("Forward path,", "/somepath", forward.getPath());
		assertEquals("Redirect status,", false, forward.getRedirect());

		// check for exposed model
		assertEquals("Model not exposed correctly,", "SomeValue", mockRequest.getAttribute("SomeKey"));
	}

	public void testToActionForwardNoRedirectViewSelectionNullForward() throws Exception {
		viewSelection.setRedirect(false);

		// perform test
		ActionForward forward = tested.toActionForward(viewSelection, nullForwardActionMapping, mockRequest);

		assertEquals("Redirect status,", false, forward.getRedirect());
		assertEquals("Forward path,", "SomeView", forward.getPath());
	}

	public void testToActionForwardRedirectViewSelectionNullForward() throws Exception {
		viewSelection.setRedirect(true);

		// perform test
		ActionForward forward = tested.toActionForward(viewSelection, nullForwardActionMapping, mockRequest);

		assertEquals("Redirect status,", true, forward.getRedirect());
		assertEquals("Forward path,", "SomeView?SomeKey=SomeValue", forward.getPath());
	}
}