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
package org.springframework.webflow.engine.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowArtifactLookupException;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.TestFlowArtifactFactory;
import org.springframework.webflow.engine.builder.XmlFlowBuilderTests;
import org.springframework.webflow.engine.builder.xml.XmlFlowBuilder;
import org.springframework.webflow.engine.machine.FlowExecutionImpl;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.support.FlowExecutionListenerLoader;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockParameterMap;

/**
 * Test case for FlowExecutionStack.
 * 
 * @see org.springframework.webflow.engine.machine.FlowExecutionImpl
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionImplTests extends TestCase {

	private FlowDefinitionLocator flowLocator;

	private FlowExecutionImpl flowExecution;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow1.xml", XmlFlowBuilderTests.class),
				new TestFlowArtifactFactory());
		FlowAssembler assembler = new FlowAssembler("testFlow", builder);
		assembler.assembleFlow();
		final Flow flow = builder.getFlow();
		flowLocator = new FlowDefinitionLocator() {
			public Flow getFlowDefinition(String flowId) throws FlowArtifactLookupException {
				if (flow.getId().equals(flowId)) {
					return flow;
				}
				throw new FlowArtifactLookupException(flowId, Flow.class);
			}
		};
		flowExecution = new FlowExecutionImpl(flow);
	}

	protected void runFlowExecutionRehydrationTest() throws Exception {
		// serialize the flowExecution
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(flowExecution);
		oout.flush();

		// deserialize the flowExecution
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(bin);
		FlowExecutionImpl restoredFlowExecution = (FlowExecutionImpl)oin.readObject();

		assertNotNull(restoredFlowExecution);

		FlowExecutionListenerLoader listenerLoader = new FlowExecutionListenerLoader() {
			public FlowExecutionListener[] getListeners(Flow flow) {
				return flowExecution.getListeners().getArray();
			}
		};
		// rehydrate the flow execution
		restoredFlowExecution.rehydrate(flowLocator, listenerLoader);

		assertEquals(flowExecution.isActive(), restoredFlowExecution.isActive());
		if (flowExecution.isActive()) {
			assertTrue(entriesCollectionsAreEqual(flowExecution.getActiveSession().getScope().getMap().entrySet(),
					restoredFlowExecution.getActiveSession().getScope().getMap().entrySet()));
			assertEquals(flowExecution.getActiveSession().getState().getFlowDefinitionId(), restoredFlowExecution.getActiveSession()
					.getState().getFlowDefinitionId());
			assertEquals(flowExecution.getActiveSession().getFlowDefinition().getFlowDefinitionId(), restoredFlowExecution.getActiveSession()
					.getFlowDefinition().getFlowDefinitionId());
			assertSame(flowExecution.getFlowDefinition(), restoredFlowExecution.getFlowDefinition());
		}
		assertEquals(flowExecution.getListeners().size(), restoredFlowExecution.getListeners().size());
	}

	public void testRehydrate() throws Exception {
		// setup some input data

		MockParameterMap input = new MockParameterMap();
		input.put("name", "value");
		// start the flow execution
		flowExecution.start(null, new MockExternalContext(input));
		runFlowExecutionRehydrationTest();
	}

	public void testRehydrateNotStarted() throws Exception {
		// don't start the flow execution
		runFlowExecutionRehydrationTest();
	}

	/**
	 * Helper to test if 2 collections of Map.Entry objects contain the same
	 * values.
	 */
	private boolean entriesCollectionsAreEqual(Collection collection1, Collection collection2) {
		if (collection1.size() != collection2.size()) {
			return false;
		}
		for (Iterator it1 = collection1.iterator(), it2 = collection2.iterator(); it1.hasNext() && it2.hasNext();) {
			Map.Entry entry1 = (Map.Entry)it1.next();
			Map.Entry entry2 = (Map.Entry)it2.next();
			if (!entry1.equals(entry2)) {
				return false;
			}
		}
		return true;
	}
}