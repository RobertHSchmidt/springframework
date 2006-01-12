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
package org.springframework.webflow.manager.struts;

import java.util.Set;

import junit.framework.TestCase;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.manager.FlowExecutionManagerImpl;
import org.springframework.webflow.manager.FlowExecutionManagerImpl.ConditionalFlowExecutionListenerHolder;
import org.springframework.webflow.manager.struts.FlowAction;
import org.springframework.webflow.manager.struts.StrutsExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the FlowAction class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowActionTests extends TestCase {
	private MockHttpServletRequest mockRequest;

	private ViewSelection viewSelection;

	private ActionForm actionForm;

	private FlowExecutionManagerImpl validViewSelectionFlowExecutionManager;

	private FlowExecutionManagerImpl nullViewSelectionFlowExecutionManager;

	private ActionMapping nullForwardActionMapping;

	private ActionMapping validForwardActionMapping;

	private FlowAction tested;

	protected void setUp() throws Exception {
		super.setUp();

		mockRequest = new MockHttpServletRequest();

		FlowLocator flowLocator = new FlowLocator() {
			public Flow getFlow(String id) throws FlowArtifactException {
				return null;
			}
		};
		viewSelection = new ViewSelection("SomeView", "SomeKey", "SomeValue");
		validViewSelectionFlowExecutionManager = new FlowExecutionManagerImpl(flowLocator) {
			public ViewSelection onEvent(ExternalContext context) {
				return viewSelection;
			}
		};
		nullViewSelectionFlowExecutionManager = new FlowExecutionManagerImpl(flowLocator) {
			public ViewSelection onEvent(ExternalContext context) {
				return null;
			}
		};
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
		actionForm = new ActionForm() {
		};
		tested = new FlowAction();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockRequest = null;
		viewSelection = null;
		validViewSelectionFlowExecutionManager = null;
		nullViewSelectionFlowExecutionManager = null;
		validForwardActionMapping = null;
		nullForwardActionMapping = null;
		actionForm = null;
		tested = null;
	}

	public void testOnInitValidFlowExecutionManager() {
		tested.setFlowExecutionManager(nullViewSelectionFlowExecutionManager);

		Set listenerSet = tested.getFlowExecutionManager().getListenerSet();
		assertTrue("should be empty before test", listenerSet.isEmpty());

		// perform test
		tested.onInit();

		listenerSet = tested.getFlowExecutionManager().getListenerSet();
		assertEquals("listeners,", 1, listenerSet.size());

		// might as well test the ActionFormAdapter, while we have it available
		SpringBindingActionForm bindingActionForm = new SpringBindingActionForm();
		StrutsExternalContext externalContext = new StrutsExternalContext(validForwardActionMapping, bindingActionForm, mockRequest, null);
		MockRequestContext mockRequestContext = new MockRequestContext(externalContext);
		// FlowAction.ActionFormAdapter is private, but we know its interface
		FlowExecutionListener listener = ((ConditionalFlowExecutionListenerHolder)listenerSet.iterator().next()).getListener();
		listener.requestProcessed(mockRequestContext);
	}

	public void testExecuteNullViewSelection() throws Exception {
		tested.setFlowExecutionManager(nullViewSelectionFlowExecutionManager);

		// perform test
		ActionForward forward = tested.execute(validForwardActionMapping, actionForm, mockRequest, null);

		assertNull(forward);
	}

	public void testExecuteValidViewSelectionValidForward() throws Exception {
		tested.setFlowExecutionManager(validViewSelectionFlowExecutionManager);

		// perform test
		ActionForward forward = tested.execute(validForwardActionMapping, actionForm, mockRequest, null);

		assertEquals("Forward name,", "SomeForward", forward.getName());
		assertEquals("Forward path,", "/somepath", forward.getPath());
		assertEquals("Redirect status,", false, forward.getRedirect());

		// check for exposed model
		assertEquals("Model not exposed correctly,", "SomeValue", mockRequest.getAttribute("SomeKey"));
	}

	public void testExecuteNoRedirectViewSelectionNullForward() throws Exception {
		tested.setFlowExecutionManager(validViewSelectionFlowExecutionManager);
		viewSelection.setRedirect(false);

		// perform test
		ActionForward forward = tested.execute(nullForwardActionMapping, actionForm, mockRequest, null);

		assertEquals("Redirect status,", false, forward.getRedirect());
		assertEquals("Forward path,", "SomeView", forward.getPath());
	}

	public void testExecuteRedirectViewSelectionNullForward() throws Exception {
		tested.setFlowExecutionManager(validViewSelectionFlowExecutionManager);
		viewSelection.setRedirect(true);

		// perform test
		ActionForward forward = tested.execute(nullForwardActionMapping, actionForm, mockRequest, null);

		assertEquals("Redirect status,", true, forward.getRedirect());
		assertEquals("Forward path,", "SomeView?SomeKey=SomeValue", forward.getPath());
	}
}