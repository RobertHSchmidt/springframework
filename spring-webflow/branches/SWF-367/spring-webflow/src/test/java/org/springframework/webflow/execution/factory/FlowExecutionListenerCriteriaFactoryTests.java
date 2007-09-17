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
package org.springframework.webflow.execution.factory;

import junit.framework.TestCase;

import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.engine.Flow;

/**
 * Unit tests for {@link FlowExecutionListenerCriteriaFactory}.
 */
public class FlowExecutionListenerCriteriaFactoryTests extends TestCase {

	private FlowExecutionListenerCriteriaFactory factory = new FlowExecutionListenerCriteriaFactory();

	public void testAllFlows() {
		FlowExecutionListenerCriteria c = factory.allFlows();
		assertEquals(true, c.appliesTo(Flow.create("foo")));
	}

	public void testFlowMatch() {
		FlowExecutionListenerCriteria c = factory.flow(FlowId.valueOf("foo"));
		assertEquals(true, c.appliesTo(Flow.create("foo")));
		assertEquals(false, c.appliesTo(Flow.create("baz")));
	}

	public void testMultipleFlowMatch() {
		FlowExecutionListenerCriteria c = factory.flows(new FlowId[] { FlowId.valueOf("foo"), FlowId.valueOf("bar") });
		assertEquals(true, c.appliesTo(Flow.create("foo")));
		assertEquals(true, c.appliesTo(Flow.create("bar")));
		assertEquals(false, c.appliesTo(Flow.create("baz")));
	}
}
