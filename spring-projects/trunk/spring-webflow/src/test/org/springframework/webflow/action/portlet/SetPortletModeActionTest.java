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
package org.springframework.webflow.action.portlet;

import javax.portlet.PortletMode;

import junit.framework.TestCase;

import org.springframework.core.ConstantException;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.webflow.ActionExecutionException;
import org.springframework.webflow.Event;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the SetPortletModeAction class.
 * 
 * @author Ulrik Sandberg
 */
public class SetPortletModeActionTest extends TestCase {

	private SetPortletModeAction tested;

	protected void setUp() throws Exception {
		super.setUp();
		tested = new SetPortletModeAction();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		tested = null;
	}

	public void testDoExecute() throws Exception {
		MockActionResponse mockActionResponse = new MockActionResponse();
		PortletExternalContext externalContext = new PortletExternalContext(null, mockActionResponse);
		MockRequestContext mockRequestContext = new MockRequestContext(externalContext);

		// perform test
		Event result = tested.doExecute(mockRequestContext);

		assertEquals(AbstractAction.SUCCESS_EVENT_ID, result.getId());
		assertEquals(tested.getPortletMode(), mockActionResponse.getPortletMode());
	}

	public void testDoExecuteWithPortletModeAsAttribute() throws Exception {
		MockActionResponse mockActionResponse = new MockActionResponse();
		PortletExternalContext externalContext = new PortletExternalContext(null, mockActionResponse);
		MockRequestContext mockRequestContext = new MockRequestContext(externalContext);
		mockRequestContext.setProperty(SetPortletModeAction.PORTLET_MODE_PROPERTY, "HELP");

		// perform test
		Event result = tested.doExecute(mockRequestContext);

		assertEquals(AbstractAction.SUCCESS_EVENT_ID, result.getId());
		assertEquals(PortletMode.HELP, mockActionResponse.getPortletMode());
	}

	public void testDoExecuteWithPortletModeAsAttributeNotString() throws Exception {
		MockActionResponse mockActionResponse = new MockActionResponse();
		PortletExternalContext externalContext = new PortletExternalContext(null, mockActionResponse);
		MockRequestContext mockRequestContext = new MockRequestContext(externalContext);
		mockRequestContext.setProperty(SetPortletModeAction.PORTLET_MODE_PROPERTY, PortletMode.HELP);

		// perform test
		try {
			tested.doExecute(mockRequestContext);
			fail("ClassCastException expected");
		} catch (ClassCastException expected) {
			// expected
		}

		assertNull("No mode should be set", mockActionResponse.getPortletMode());
	}

	public void testDoExecuteWithWrongResponseClass() throws Exception {
		MockRenderResponse mockRenderResponse = new MockRenderResponse();
		PortletExternalContext externalContext = new PortletExternalContext(null, mockRenderResponse);
		MockRequestContext mockRequestContext = new MockRequestContext(externalContext);
		mockRequestContext.setProperty(SetPortletModeAction.PORTLET_MODE_PROPERTY, PortletMode.HELP);

		// perform test
		try {
			tested.doExecute(mockRequestContext);
			fail("ActionExecutionException expected");
		} catch (ActionExecutionException expected) {
			// expected
		}
	}

	public void testSetPortletModeAsString() {
		assertEquals("Before change,", PortletMode.VIEW, tested.getPortletMode());

		// perform test
		tested.setPortletModeAsString("EDIT");

		assertEquals("After change,", PortletMode.EDIT, tested.getPortletMode());
	}

	public void testSetPortletModeAsStringInvalid() {
		assertEquals("Before,", PortletMode.VIEW, tested.getPortletMode());

		// perform test
		try {
			tested.setPortletModeAsString("NONSENSE");
			fail("ConstantException expected");
		} catch (ConstantException expected) {
			// expected
		}

		assertEquals("Should not have changed,", PortletMode.VIEW, tested.getPortletMode());
	}

	public void testSetPortletModeAsStringNull() {
		assertEquals("Before,", PortletMode.VIEW, tested.getPortletMode());

		// perform test
		try {
			tested.setPortletModeAsString(null);
			fail("NullPointerException expected");
		} catch (NullPointerException expected) {
			// expected
		}

		assertEquals("Should not have changed,", PortletMode.VIEW, tested.getPortletMode());
	}
}
