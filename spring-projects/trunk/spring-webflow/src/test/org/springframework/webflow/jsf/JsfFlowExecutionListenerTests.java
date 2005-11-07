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

import java.util.HashMap;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the JsfFlowExecutionListener class.
 * 
 * @author Ulrik Sandberg
 */
public class JsfFlowExecutionListenerTests extends TestCase {

	private MockRequestContext mockRequestContext;

	private JsfFlowExecutionListener tested;

	protected void setUp() throws Exception {
		super.setUp();
		mockRequestContext = new MockRequestContext();

		MockExternalContext mockExternalContext = new MockExternalContext();
		HashMap requestMap = new HashMap();
		requestMap.put("SomeKey", "SomeValue");
		mockExternalContext.setRequestMap(requestMap);

		final MockFacesContext mockFacesContext = new MockFacesContext();
		mockFacesContext.setExternalContext(mockExternalContext);
		JsfEvent jsfEvent = new JsfEvent("SomeOutcome", mockFacesContext, "SomeAction") {

			public FacesContext getFacesContext() {
				return mockFacesContext;
			}
		};
		mockRequestContext.setSourceEvent(jsfEvent);
		tested = new JsfFlowExecutionListener();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockRequestContext = null;
		tested = null;
	}

	public void testRequestSubmitted() {
		// perform test
		tested.requestSubmitted(mockRequestContext);

		Object attribute = mockRequestContext.getRequestScope().getAttribute("SomeKey");
		assertNotNull("request map not saved", attribute);
		assertEquals("SomeValue", attribute);
	}
}