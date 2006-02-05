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
package org.springframework.webflow.test;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.webflow.Flow;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;
import org.springframework.webflow.registry.ExternalizedFlowRegistrar;
import org.springframework.webflow.registry.FlowRegistry;
import org.springframework.webflow.registry.StaticFlowHolder;

/**
 * Base class for flow integration tests that verify an externalized flow
 * definition executes as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractExternalizedFlowExecutionTests extends AbstractManagedFlowExecutionTests {

	/**
	 * Simply tracks the <code>id</code> of the flow definition to test.
	 */
	private static String flowId;

	/**
	 * The cached flows.
	 */
	private static Collection cachedFlows;
	
	/**
	 * A flag indicating if this test should cache flow definitions built from
	 * externalized resources.  Default is <code>true</code>.
	 */
	private boolean cacheFlowDefinitions = false;

	protected void populateFlowRegistry(FlowRegistry flowRegistry, FlowArtifactFactory flowArtifactFactory) {
		if (isCachePopulated()) {
			Iterator it = cachedFlows.iterator();
			while (it.hasNext()) {
				flowRegistry.registerFlow(new StaticFlowHolder((Flow)it.next()));
			}
		}
		else {
			ExternalizedFlowRegistrar registrar = createFlowRegistrar();
			ExternalizedFlowDefinition flowDefinition = getFlowDefinition();
			flowId = flowDefinition.getId();
			registrar.addFlowDefinition(flowDefinition);
			registrar.addFlowDefinitions(getSubflowDefinitions());
			registrar.registerFlows(getFlowRegistry(), getFlowArtifactFactory());
			if (isCacheFlowDefinitions()) {
				// cache externally-built flow definitions for performance
				cachedFlows = flowRegistry.getFlows();
			}
		}
	}

	/**
	 * Returns whether this test is caching the flow definitions built from the
	 * underlying externalized resources.
	 */
	public boolean isCacheFlowDefinitions() {
		return cacheFlowDefinitions;
	}
	/**
	 * Set the flag indicating if this test should cache the flow definitions
	 * built from the referenced externalized resources.
	 */
	public void setCacheFlowDefinitions(boolean cacheFlowDefinitions) {
		this.cacheFlowDefinitions = cacheFlowDefinitions;
	}

	/**
	 * Returns if the cache of flow definitions has been populated; only could return 
	 * true if caching has been turned on.
	 */
	public boolean isCachePopulated() {
		return isCacheFlowDefinitions() && cachedFlows != null;
	}
	
	protected final String getFlowId() {
		return flowId;
	}

	/**
	 * Factory method that returns the flow registrar that will perform registry
	 * population of externalized flow definitions. Sublcasses may override.
	 */
	protected abstract ExternalizedFlowRegistrar createFlowRegistrar();

	/**
	 * Returns the definition of the externalized flow needed by this flow
	 * execution test: subclasses must override.
	 * <p>
	 * The Flow definitions store returned is automatically added to this test's
	 * FlowRegistry by the
	 * {@link #populateFlowRegistry(FlowRegistry, FlowArtifactFactory)} method,
	 * called on test setup.
	 * @return the externalize flow definition to test
	 */
	protected abstract ExternalizedFlowDefinition getFlowDefinition();

	/**
	 * Returns the array of definitions pointing to the externalized flows used
	 * as subflows in this flow execution test. Optional. This default
	 * implementation returns <code>null</code>, assuming there are no
	 * subflows spawned in the flow execution being tested.
	 * <p>
	 * Flow definitions stored in the returned resouce array are automatically
	 * added to this test's FlowRegistry by the {@link #populateFlowRegistry}
	 * method, called on test setup.
	 * @return the externalized flow definitions needed as subflows by this test
	 */
	protected ExternalizedFlowDefinition[] getSubflowDefinitions() {
		return null;
	}
}