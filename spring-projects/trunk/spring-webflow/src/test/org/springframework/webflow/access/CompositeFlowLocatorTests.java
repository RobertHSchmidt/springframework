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
package org.springframework.webflow.access;

import junit.framework.TestCase;

import org.springframework.webflow.Flow;

/**
 * Unit test for the CompositeFlowLocator class.
 * 
 * @author Ulrik Sandberg
 */
public class CompositeFlowLocatorTests extends TestCase {
	public void testGetFlowLookupProblem() throws Exception {
		FlowLocator flowLocator = new FlowLocator() {
			public Flow getFlow(String id) throws ArtifactLookupException {
				throw new ArtifactLookupException(Object.class, "SomeArtifact");
			}

		};
		CompositeFlowLocator tested = new CompositeFlowLocator(new FlowLocator[] { flowLocator });
		// perform test
		try {
			tested.getFlow("SomeFlow");
			fail("FlowArtifactLookupException expected");
		}
		catch (ArtifactLookupException expected) {
			// expected
		}
	}

	public void testGetFlow() throws Exception {
		FlowLocator flowLocator = new FlowLocator() {
			public Flow getFlow(String id) throws ArtifactLookupException {
				return new Flow(id);
			}

		};
		CompositeFlowLocator tested = new CompositeFlowLocator(new FlowLocator[] { flowLocator });
		// perform test
		Flow flow = tested.getFlow("SomeFlow");
		assertEquals("Flow id,", "SomeFlow", flow.getId());
	}
}