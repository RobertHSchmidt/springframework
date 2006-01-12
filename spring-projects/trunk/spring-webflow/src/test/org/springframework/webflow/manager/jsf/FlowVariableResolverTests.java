/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.manager.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.manager.jsf.FlowExecutionHolder;
import org.springframework.webflow.manager.jsf.FlowVariableResolver;

/**
 * Unit tests for the FlowVariableResolver class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowVariableResolverTests extends TestCase {

	private FlowVariableResolver tested;

	private TestableVariableResolver resolver;

	private MockFacesContext context;

	protected void setUp() throws Exception {
		super.setUp();
		context = new MockFacesContext();
		resolver = new TestableVariableResolver();
		tested = new FlowVariableResolver(resolver);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		context = null;
		resolver = null;
		tested = null;
	}

	public void testResolveVariableNotFlowScope() {
		Object result = tested.resolveVariable(context, "some name");
		assertTrue("not resolved using delegate", resolver.resolvedUsingDelegate);
		assertSame(resolver.expected, result);
	}

	public void testResolveVariableFlowScopeWithNoThreadLocal() {
		try {
			tested.resolveVariable(context, "flowScope");
			fail("EvaluationException expected");
		}
		catch (EvaluationException expected) {
			assertEquals(
					"'flowScope' variable prefix specified, but a FlowExecution is not bound to current thread context as it should be",
					expected.getMessage());
		}
		assertFalse("resolved using delegate", resolver.resolvedUsingDelegate);
	}

	public void testResolveVariableFlowScopeWithThreadLocal() {
		MockControl flowExecutionControl = MockControl.createControl(FlowExecution.class);
		FlowExecution flowExecutionMock = (FlowExecution)flowExecutionControl.getMock();
		FlowExecutionContinuationKey key = new FlowExecutionContinuationKey("some conversation id",
				"some continuation id");
		FlowExecutionHolder.setFlowExecutionHolder(key, flowExecutionMock);
		flowExecutionControl.replay();

		Object result = tested.resolveVariable(context, "flowScope");

		flowExecutionControl.verify();
		assertFalse("resolved using delegate", resolver.resolvedUsingDelegate);
		assertSame(flowExecutionMock, result);
	}

	private static class TestableVariableResolver extends VariableResolver {
		private boolean resolvedUsingDelegate;

		private Object expected = new Object();

		public Object resolveVariable(FacesContext arg0, String arg1) throws EvaluationException {
			resolvedUsingDelegate = true;
			return expected;
		}
	}
}
