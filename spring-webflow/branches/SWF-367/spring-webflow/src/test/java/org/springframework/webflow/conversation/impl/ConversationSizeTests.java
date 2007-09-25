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
package org.springframework.webflow.conversation.impl;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.springframework.webflow.config.FlowExecutorFactoryBean;
import org.springframework.webflow.config.FlowExecutionRepositoryType;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.AbstractFlowBuilder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.execution.support.FlowExecutionRedirect;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.test.MockExternalContext;

/**
 * Test case that looks for the miminum conversation size.
 */
public class ConversationSizeTests extends TestCase {

	private SessionBindingConversationManager conversationManager;

	private FlowExecutor flowExecutor;

	protected void setUp() throws Exception {
		FlowDefinitionRegistry flowRegistry = new FlowDefinitionRegistryImpl();
		new SizeTestFlowRegistrar().registerFlowDefinitions(flowRegistry);

		conversationManager = new SessionBindingConversationManager();

		FlowExecutorFactoryBean flowExecutorFactory = new FlowExecutorFactoryBean();
		flowExecutorFactory.setFlowDefinitionLocator(flowRegistry);
		flowExecutorFactory.setConversationManager(conversationManager);
		flowExecutorFactory.setFlowExecutionRepositoryType(FlowExecutionRepositoryType.CONTINUATION);
		flowExecutorFactory.afterPropertiesSet();
		flowExecutor = flowExecutorFactory.getFlowExecutor();
	}

	public void testConversationSize() throws Exception {
		MockExternalContext context = new MockExternalContext();
		SharedAttributeMap session = context.getSessionMap();

		// initially the session is empty
		assertTrue(session.isEmpty());

		ResponseInstruction ri = flowExecutor.launch("size-test-flow", context);
		assertTrue(ri.getFlowExecutionContext().isActive());
		assertTrue(ri.getViewSelection() instanceof FlowExecutionRedirect); // alwaysRedirectOnPause

		// the launch has stored a ConversationContainer in the session since we're using
		// SessionBindingConversationManager
		assertEquals(1, session.size());
		ConversationContainer conversationContainer = (ConversationContainer) session.get(conversationManager
				.getSessionKey());
		assertNotNull(conversationContainer);
		assertEquals(1, conversationContainer.size());
		int initialSize = getSerializedSize(conversationContainer);

		ri = flowExecutor.refresh(ri.getFlowExecutionKey(), context);
		assertTrue(ri.getFlowExecutionContext().isActive());
		assertEquals("view", ((ApplicationView) ri.getViewSelection()).getViewName());

		// the refresh did not impact the size of the session
		assertEquals(1, session.size());
		assertSame(conversationContainer, session.get(conversationManager.getSessionKey()));
		assertEquals(1, conversationContainer.size());

		ri = flowExecutor.resume(ri.getFlowExecutionKey(), "end", context);
		assertFalse(ri.getFlowExecutionContext().isActive());
		assertTrue(ri.isNull());

		// the conversation ended but the ConversationContainer is still in the session
		assertEquals(1, session.size());
		assertSame(conversationContainer, session.get(conversationManager.getSessionKey()));
		assertEquals(0, conversationContainer.size());
		int inactiveSize = getSerializedSize(conversationContainer);

		assertTrue(inactiveSize < initialSize);
	}

	// helpers

	private int getSerializedSize(Object obj) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(obj);
		oout.flush();
		int objSize = bout.toByteArray().length;
		return objSize;
	}

	private static class SizeTestFlowBuilder extends AbstractFlowBuilder {
		public void buildStates() throws FlowBuilderException {
			addViewState("view", "view", transition(on("end"), to("end")));
			addEndState("end");
		}
	}

	private static class SizeTestFlowRegistrar implements FlowDefinitionRegistrar {

		public void registerFlowDefinitions(FlowDefinitionRegistry registry) {
			Flow flow = new FlowAssembler("size-test-flow", null, new SizeTestFlowBuilder()).assembleFlow();
			FlowDefinitionHolder flowHolder = new StaticFlowDefinitionHolder(flow);
			registry.registerFlowDefinition(flowHolder);
		}
	}
}
