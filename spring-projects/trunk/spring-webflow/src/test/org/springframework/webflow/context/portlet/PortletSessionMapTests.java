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
package org.springframework.webflow.context.portlet;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.springframework.mock.web.portlet.MockPortletRequest;

/**
 * Unit test for the PortletSessionMap class.
 * 
 * @author Ulrik Sandberg
 */
public class PortletSessionMapTests extends TestCase {

	private PortletSessionMap tested;
	private MockPortletRequest mockPortletRequest;

	protected void setUp() throws Exception {
		super.setUp();
		mockPortletRequest = new MockPortletRequest();
		tested = new PortletSessionMap(mockPortletRequest);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mockPortletRequest = null;
		tested = null;
	}

	public void testGetAttribute() {
		mockPortletRequest.getPortletSession().setAttribute("Some key", "Some value");
		// perform test
		Object result = tested.getAttribute("Some key");
		assertEquals("Some value", result);
	}

	public void testGetAttributeNullSession() {
		mockPortletRequest.setSession(null);
		// perform test
		Object result = tested.getAttribute("Some key");
		assertNull("No value expected", result);
	}

	public void testSetAttribute() {
		// perform test
		tested.setAttribute("Some key", "Some value");
		assertEquals("Some value", mockPortletRequest.getPortletSession().getAttribute("Some key"));
	}

	public void testRemoveAttribute() {
		mockPortletRequest.getPortletSession().setAttribute("Some key", "Some value");
		// perform test
		tested.removeAttribute("Some key");
		assertNull(mockPortletRequest.getPortletSession().getAttribute("Some key"));
	}

	public void testRemoveAttributeNullSession() {
		mockPortletRequest.setSession(null);
		// perform test
		tested.removeAttribute("Some key");
		assertNull(mockPortletRequest.getPortletSession().getAttribute("Some key"));
	}

	public void testGetAttributeNames() {
		mockPortletRequest.getPortletSession().setAttribute("Some key", "Some value");
		// perform test
		Enumeration names = tested.getAttributeNames();
		assertNotNull("Null result unexpected", names);
		assertTrue("More elements", names.hasMoreElements());
		String name = (String) names.nextElement();
		assertEquals("Some key", name);
	}

	public void testGetAttributeNamesNullSession() {
		mockPortletRequest.setSession(null);
		// perform test
		Enumeration names = tested.getAttributeNames();
		assertNotNull("Null result unexpected", names);
		assertFalse("No elements expected", names.hasMoreElements());
	}
}