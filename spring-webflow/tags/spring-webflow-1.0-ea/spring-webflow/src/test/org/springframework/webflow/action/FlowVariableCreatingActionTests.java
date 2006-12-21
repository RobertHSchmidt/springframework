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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.support.FlowVariable;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for the FlowVariableCreatingAction class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowVariableCreatingActionTests extends TestCase {

	private FlowVariableCreatingAction tested;

	protected void setUp() throws Exception {
		super.setUp();
		tested = new FlowVariableCreatingAction(new FlowVariable("Some variable", Object.class));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		tested = null;
	}

	public void testDoExecute() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		Event result = tested.doExecute(mockRequestContext);
		assertNotNull(result);
	}

	public void testConstructorWithArray() throws Exception {
		FlowVariable[] expectedVariables = new FlowVariable[] { new FlowVariable("Some variable", Object.class) };
		FlowVariableCreatingAction localTested = new FlowVariableCreatingAction(expectedVariables);
		FlowVariable[] actualVariables = localTested.getVariables();
		assertEquals(1, actualVariables.length);
	}
}