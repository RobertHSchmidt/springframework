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

import org.springframework.core.io.Resource;
import org.springframework.webflow.FlowLocator;
import org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean;

/**
 * Base class for integration tests that verify a XML flow definition executes
 * as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractXmlFlowExecutionTests extends AbstractFlowExecutionTests {

	/*
	 * Overrides to return a FlowRegistry populated from a set of XML flow
	 * definition resources.
	 * @see org.springframework.webflow.test.AbstractFlowExecutionTests#createFlowLocator()
	 */
	protected FlowLocator createFlowLocator() {
		XmlFlowRegistryFactoryBean factory = new XmlFlowRegistryFactoryBean(applicationContext);
		factory.setDefinitionLocations(getFlowLocations());
		return factory.populateFlowRegistry();
	}

	/**
	 * Returns the array of resources pointing to the XML-based flow definitions
	 * needed by this flow execution test: subclasses must override.
	 * <p>
	 * Flow definitions stored in the returned resouce array are automatically
	 * added to this test's FlowRegistry by the {@link #createFlowLocator}
	 * method, called on test setup.
	 * @return the locations of the XML flow definitions needed by this test
	 */
	protected abstract Resource[] getFlowLocations();
}