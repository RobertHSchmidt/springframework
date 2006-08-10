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
package org.springframework.webflow.test.execution.engine;

import org.springframework.core.io.Resource;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowServiceLocator;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.test.execution.AbstractFlowExecutionTests;

/**
 * Base class for flow integration tests that verify an externalized flow
 * definition executes as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractExternalizedFlowExecutionTests extends AbstractFlowExecutionTests {

	protected final FlowExecutionFactory createFlowExecutionFactory() {
		return new MockFlowExecutionFactory();
	}

	/**
	 * Returns the flow artifact factory to use during flow definition
	 * construction time for accessing externally managed flow artifacts such as
	 * actions and flows to be used as subflows.
	 * <p>
	 * Subclasses should override to return a specific flow artifact factory
	 * implementation to support their flow execution test scenarios.
	 * 
	 * @return the flow artifact factory
	 */
	protected FlowServiceLocator createFlowServiceLocator() {
		MockFlowServiceLocator serviceLocator = new MockFlowServiceLocator();
		registerMockServices(serviceLocator);
		return serviceLocator;
	}

	/**
	 * Template method called by {@link #createFlowServiceLocator()} to ease the
	 * registration of mock implementations of services needed to test the flow
	 * execution. Subclasses may override.
	 * @param serviceLocator the mock service locator
	 */
	protected void registerMockServices(MockFlowServiceLocator serviceLocator) {
	}

	/**
	 * Create the builder that will build the flow whose execution will be
	 * tested.
	 * @param resource the externalized flow definition resource location
	 * @param flowServiceLocator the flow artifact factory
	 * @return the flow builder
	 */
	protected abstract FlowBuilder createFlowBuilder(Resource resource, FlowServiceLocator flowServiceLocator);

}