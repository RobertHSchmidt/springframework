/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.config.scope;

import junit.framework.TestCase;

import org.springframework.webflow.execution.FlowExecutionContextHolder;
import org.springframework.webflow.test.MockFlowExecutionContext;

/**
 * Test cases for the
 * @{link FlowScope} class.
 * 
 * @author Ben Hale
 */
public class FlowScopeTests extends TestCase {

	private MockFlowExecutionContext context;

	private FlowScope scope;

	protected void setUp() {
		context = new MockFlowExecutionContext();
		FlowExecutionContextHolder.setFlowExecutionContext(context);
		scope = new FlowScope();
	}

	protected void tearDown() {
		scope = null;
		context = null;
		FlowExecutionContextHolder.setFlowExecutionContext(null);
	}

	public void testGetVarMissing() {
		StubObjectFactory factory = new StubObjectFactory();
		Object gotten = scope.get("name", factory);
		assertNotNull("Should be real object", gotten);
		assertTrue("Should have added object to the map", context.getActiveSession().getScope().contains("name"));
		assertSame("Created object should have been returned", factory.getValue(), gotten);
		assertSame("Created object should have been persisted", factory.getValue(), context.getActiveSession()
				.getScope().get("name"));
	}

	public void testGetVarExist() {
		StubObjectFactory factory = new StubObjectFactory();
		Object value = new Object();
		context.getActiveSession().getScope().put("name", value);
		Object gotten = scope.get("name", factory);
		assertNotNull("Should be real object", gotten);
		assertTrue("Should still be in map", context.getActiveSession().getScope().contains("name"));
		assertSame("Persisted object should have been returned", value, gotten);
		assertNotSame("Created object should not have been returned", factory.getValue(), gotten);
	}

	public void testGetRequestContextMissing() {
		FlowExecutionContextHolder.setFlowExecutionContext(null);
		StubObjectFactory factory = new StubObjectFactory();
		try {
			scope.get("name", factory);
		} catch (IllegalStateException e) {
		}
	}

	public void testGetConversationId() {
		String flowId = scope.getConversationId();
		assertNull("Method not implemented yet, should return null", flowId);
	}

	public void testRemoveVarMissing() {
		Object removed = scope.remove("name");
		assertFalse("Should have removed from object from map", context.getActiveSession().getScope().contains("name"));
		assertNull("Should have returned a null object", removed);
	}

	public void testRemoveVarExist() {
		Object value = new Object();
		context.getActiveSession().getScope().put("name", value);
		Object removed = scope.remove("name");
		assertFalse("Should have removed from object from map", context.getActiveSession().getScope().contains("name"));
		assertSame("Should have returned the previous object", removed, value);
	}

	public void testRemoveRequestContextMissing() {
		FlowExecutionContextHolder.setFlowExecutionContext(null);
		try {
			scope.remove("name");
			fail("Should have thrown a IllegalStateException without a request context");
		} catch (IllegalStateException e) {
		}
	}

}
