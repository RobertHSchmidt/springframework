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
package org.springframework.webflow.engine;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

/**
 * Tests dealing with nested flows.
 * 
 * @author Erwin Vervaet
 */
public class NestedFlowTests extends AbstractXmlFlowExecutionTests {

	protected FlowDefinitionResource getFlowDefinitionResource() {
		return new FlowDefinitionResource(new ClassPathResource("testFlow.xml", NestedFlowTests.class));
	}
	
	public void testNestedFlows() {
		startFlow();
		assertFlowExecutionActive();
		assertActiveFlowEquals("testFlow");
		assertCurrentStateEquals("view1");
		signalEvent("start");
		assertFlowExecutionActive();
		assertActiveFlowEquals("subflowDef3");
		assertCurrentStateEquals("view4");
		signalEvent("continue");
		assertFlowExecutionEnded();
	}

}
