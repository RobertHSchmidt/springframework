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
package org.springframework.webflow.test;

import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistry;
import org.springframework.webflow.registry.FlowRegistryFlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistryImpl;

/**
 * Base class for flow integration tests that manage flow definitions to be
 * tested in an explicit Flow registry.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFlowExecutionTests extends AbstractFlowExecutionTests {

	/**
	 * The flow registry.
	 */
	private static FlowRegistry flowRegistry;

	/**
	 * The flow artifact factory.
	 */
	private static FlowArtifactFactory flowArtifactFactory;

	/**
	 * Returns the flow artifact factory.
	 */
	protected static FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	/**
	 * Returns the flow registry.
	 */
	protected static FlowRegistry getFlowRegistry() {
		return flowRegistry;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.webflow.test.AbstractFlowExecutionTests#getFlow()
	 */
	protected Flow getFlow() throws FlowArtifactException {
		return flowRegistry.getFlow(flowId());
	}

	/**
	 * Returns the <code>id</code> of the flow this execution test should
	 * test. By default, returns the first entry in the flow registry.
	 * Subclasses may override.
	 */
	protected String flowId() {
		return getFlowRegistry().getFlowIds()[0];
	}

	protected void onSetUpInTransactionalFlowTest() {
		if (flowRegistry == null) {
			initFlowRegistry();
			initFlowArtifactFactory();
			populateFlowRegistry();
		}
	}

	/**
	 * Initialize the flow registry.
	 */
	protected void initFlowRegistry() {
		flowRegistry = new FlowRegistryImpl();
	}

	/**
	 * Initialize the flow artifact factory.
	 */
	protected void initFlowArtifactFactory() {
		flowArtifactFactory = new FlowRegistryFlowArtifactFactory(flowRegistry, applicationContext);
	}

	protected abstract void populateFlowRegistry();

}