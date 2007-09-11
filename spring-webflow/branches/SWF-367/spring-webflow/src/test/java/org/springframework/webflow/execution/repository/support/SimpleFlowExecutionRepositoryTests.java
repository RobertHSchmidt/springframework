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
package org.springframework.webflow.execution.repository.support;

import junit.framework.TestCase;

import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.StubViewFactory;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.execution.repository.PermissionDeniedFlowExecutionAccessException;
import org.springframework.webflow.test.MockExternalContext;

/**
 * Unit tests for {@link SimpleFlowExecutionRepository}.
 */
public class SimpleFlowExecutionRepositoryTests extends TestCase {

	private SimpleFlowExecutionRepository repository;

	private FlowExecution execution;

	private FlowExecutionKey key;

	private FlowExecutionLock lock;

	protected void setUp() throws Exception {
		final Flow flow = new Flow("flow");
		ViewState view = new ViewState(flow, "view", new StubViewFactory());
		FlowDefinitionLocator flowLocator = new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowPath) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return flow;
			}
		};
		execution = new FlowExecutionImplFactory().createFlowExecution(flow);
		FlowExecutionStateRestorer stateRestorer = new FlowExecutionImplStateRestorer(flowLocator);
		repository = new SimpleFlowExecutionRepository(new SessionBindingConversationManager(), stateRestorer);
		ExternalContextHolder.setExternalContext(new MockExternalContext());
	}

	public void testPutExecution() {
		key = repository.generateKey(execution);
		assertNotNull(key);
		lock = repository.getLock(key);
		lock.lock();
		repository.putFlowExecution(key, execution);
		FlowExecution persisted = repository.getFlowExecution(key);
		assertNotNull(persisted);
		assertSame(execution, persisted);
		lock.unlock();
	}

	public void testGetNextKey() {
		key = repository.generateKey(execution);
		assertNotNull(key);
		lock = repository.getLock(key);
		lock.lock();
		repository.putFlowExecution(key, execution);
		FlowExecutionKey nextKey = repository.getNextKey(execution, key);
		repository.putFlowExecution(nextKey, execution);
		FlowExecution persisted = repository.getFlowExecution(nextKey);
		assertNotNull(persisted);
		assertSame(execution, persisted);
		lock.unlock();
	}

	public void testGetNextKeyVerifyKeyChanged() {
		key = repository.generateKey(execution);
		assertNotNull(key);
		lock = repository.getLock(key);
		lock.lock();
		repository.putFlowExecution(key, execution);
		FlowExecutionKey nextKey = repository.getNextKey(execution, key);
		repository.putFlowExecution(nextKey, execution);
		try {
			repository.getFlowExecution(key);
			fail("Should've failed");
		} catch (PermissionDeniedFlowExecutionAccessException e) {
		}
		lock.unlock();
	}

	public void testGetNextKeyVerifyKeyStaysSame() {
		repository.setAlwaysGenerateNewNextKey(false);
		key = repository.generateKey(execution);
		FlowExecutionKey nextKey = repository.getNextKey(execution, key);
		assertSame(key, nextKey);
	}

	public void testRemove() {
		testPutExecution();
		lock.lock();
		repository.removeFlowExecution(key);
		try {
			repository.getFlowExecution(key);
			fail("should've throw nsfee");
		} catch (NoSuchFlowExecutionException e) {
		}
		lock.unlock();
	}

	public void testLock() {
		testPutExecution();
		FlowExecutionLock lock = repository.getLock(key);
		lock.lock();
		repository.getFlowExecution(key);
		lock.unlock();
	}

	public void testLockLock() {
		testPutExecution();
		FlowExecutionLock lock = repository.getLock(key);
		lock.lock();
		lock.lock();
		repository.getFlowExecution(key);
		lock.unlock();
		lock.unlock();
	}
}