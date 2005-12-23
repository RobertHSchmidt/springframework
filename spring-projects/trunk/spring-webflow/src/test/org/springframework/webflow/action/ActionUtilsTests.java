/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for the ActionUtils class.
 * 
 * @author Ulrik Sandberg
 */
public class ActionUtilsTests extends TestCase {

	public void testGetRequiredActionProperty() {
		MockRequestContext mockRequestContext = new MockRequestContext();
		mockRequestContext.setProperty("SomeKey", "SomeValue");
		String result = (String)ActionUtils.getRequiredActionProperty(mockRequestContext, "SomeKey");
		assertEquals("SomeValue", result);
	}

	public void testGetRequiredActionPropertyNone() {
		MockRequestContext mockRequestContext = new MockRequestContext();
		try {
			ActionUtils.getRequiredActionProperty(mockRequestContext, "SomeKey");
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException expected) {
			assertEquals(
					"Required action execution property 'SomeKey' not present in request context, properties present are: {}",
					expected.getMessage());
		}
	}
}