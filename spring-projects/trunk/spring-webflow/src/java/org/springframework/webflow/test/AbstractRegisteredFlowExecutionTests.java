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
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistry;
import org.springframework.webflow.registry.FlowRegistryFlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistryImpl;

/**
 * Base class for integration tests that verify a XML flow definition executes
 * as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractRegisteredFlowExecutionTests extends AbstractFlowExecutionTests {

	private static FlowRegistry flowRegistry;

	private static FlowArtifactFactory flowArtifactFactory;
	
	public static FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	public static FlowRegistry getFlowRegistry() {
		return flowRegistry;
	}

	protected Flow getFlow() throws FlowArtifactLookupException {
  		return flowRegistry.getFlow(flowId());
	}
	
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

	protected void initFlowRegistry() {
		flowRegistry = new FlowRegistryImpl();
	}
	
	protected void initFlowArtifactFactory() {
		flowArtifactFactory = new FlowRegistryFlowArtifactFactory(flowRegistry, applicationContext);
	}
	
	protected abstract void populateFlowRegistry();

}