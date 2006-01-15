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

import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistry;
import org.springframework.webflow.registry.FlowRegistryFlowArtifactFactory;
import org.springframework.webflow.registry.FlowRegistryImpl;

/**
 * Base class for flow integration tests that test the execution of a managed
 * flow definition source from a {@link FlowRegistry}.
 * <p>
 * Subclasses should override
 * {@link #populateFlowRegistry(FlowRegistry, FlowArtifactFactory)} to  load the
 * registry with flow definitions necessary to support this test. Exactly one of
 * those flow definitions must be registered with an <code>id</code> that
 * matches the id returned by {@link #getFlowId()}, selecting the flow whose
 * execution is to be tested by this test (other registered flows would be
 * subflows spawned by that flow during test execution).
 * <p>
 * Example usage in a subclass demonstrating use of a custom FlowRegistrar to
 * populate the test's flow registry:
 * <pre>
 * public class SearchFlowExecutionTests extends AbstractManagedFlowExecutionTests {
 * 
 *     // the registry id of the flow execution to test
 *     protected String getFlowId() {
 *         return "search";
 *     }
 *     
 *     // populate the registry using a custom registrar
 *     protected void populateFlowRegistry(FlowRegistry flowRegistry, FlowArtifactFactory flowArtifactFactory) {
 *         new PhonebookFlowRegistrar().registerFlows(flowRegistry, flowArtifactFactory);
 *     }
 *     
 *     protected String[] getConfigLocations() {
 *         return new String[] {
 *             "classpath:org/springframework/webflow/samples/phonebook/deploy/service-layer.xml",
 *             "classpath:org/springframework/webflow/samples/phonebook/deploy/web-layer.xml"
 *         };
 *     }
 *
 *     public void testStartFlow() {
 *         startFlow();
 *         assertCurrentStateEquals("displayCriteria");
 *     }
 *     
 *     ...
 * }
 * 
 * public static class PhonebookFlowRegistrar extends FlowRegistrarSupport {
 *     public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
 *         registerFlow("search", new SearchPersonFlowBuilder(flowArtifactFactory), registry);
 *         registerFlow("detail", new PersonDetailFlowBuilder(flowArtifactFactory), registry);
 *     }
 * }
 * </pre>
 * 
 * @author Keith Donald
 */
public abstract class AbstractManagedFlowExecutionTests extends AbstractFlowExecutionTests {

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
		return flowRegistry.getFlow(getFlowId());
	}

	/**
	 * Returns the <code>id</code> of the flow this execution test should
	 * test. Subclasses must override.
	 */
	protected abstract String getFlowId();

	protected void onSetUpInTransactionalFlowTest() {
		if (flowRegistry == null) {
			initFlowRegistry();
			initFlowArtifactFactory();
			populateFlowRegistry(flowRegistry, flowArtifactFactory);
		}
	}

	/**
	 * Initialize the flow registry to support this test.
	 */
	protected void initFlowRegistry() {
		flowRegistry = new FlowRegistryImpl();
	}

	/**
	 * Initialize the flow artifact factory to support this test.
	 */
	protected void initFlowArtifactFactory() {
		flowArtifactFactory = new FlowRegistryFlowArtifactFactory(flowRegistry, applicationContext);
	}

	/**
	 * Performs flow registry population, registering the flow to be tested as
	 * well as any subflows. Implementations will typically delegate to a
	 * FlowRegistrar. Subclasses must override.
	 * @param flowRegistry the registry to populate with flow definitions to
	 * support this test
	 * @param flowArtifactFactory a application context-backed flow artifact
	 * factory that is used to load the configuration of flow definition
	 * artifacts during test execution.
	 */
	protected abstract void populateFlowRegistry(FlowRegistry flowRegistry, FlowArtifactFactory flowArtifactFactory);

}