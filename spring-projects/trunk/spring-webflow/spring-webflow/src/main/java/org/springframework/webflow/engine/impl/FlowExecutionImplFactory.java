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
package org.springframework.webflow.engine.impl;

import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowArtifactLookupException;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.execution.factory.support.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.support.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.registry.FlowLocator;

/**
 * A factory for the default flow execution implementation.
 * 
 * @author Keith Donald
 */
public class FlowExecutionImplFactory implements FlowExecutionFactory {

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client.
	 */
	private FlowLocator flowLocator;

	/**
	 * The strategy for loading listeners that should observe executions of a flow definition.
	 * The default simply loads an empty static listener list.
	 */
	private FlowExecutionListenerLoader listenerLoader = new StaticFlowExecutionListenerLoader();

	/**
	 * Creates a new flow execution impl factory which locates flow definitions using the 
	 * provided locator.
	 * @param flowLocator the flow locator
	 */
	public FlowExecutionImplFactory(FlowLocator flowLocator) {
		Assert.notNull(flowLocator, "The flow locator is required");
		this.flowLocator = flowLocator;
	}

	/**
	 * Sets the strategy for loading listeners that should observe executions of a flow definition.
	 */
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		Assert.notNull(listenerLoader, "The listener loader is required");
		this.listenerLoader = listenerLoader;	
	}

	public FlowExecution createFlowExecution(String flowId) {
		FlowDefinition flow = flowLocator.getFlow(flowId);
		return new FlowExecutionImpl((Flow)flowLocator.getFlow(flowId), listenerLoader.getListeners(flow));
	}

	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope) {
		FlowExecutionImpl impl = (FlowExecutionImpl)flowExecution;
		Flow flow = (Flow)flowLocator.getFlow(impl.flowId);
		Iterator it = impl.flowSessions.iterator();
		FlowSessionFlowLocator locator = new FlowSessionFlowLocator(flow, flowLocator);
		while (it.hasNext()) {
			FlowSessionImpl session = (FlowSessionImpl)it.next();
			session.flow = (Flow)locator.getFlow(session.flowId);
			session.state = flow.getRequiredState(session.stateId);
		}
		impl.listeners = new FlowExecutionListeners(listenerLoader.getListeners(flow));
		impl.conversationScope = conversationScope;
		return flowExecution;
	}

	private static class FlowSessionFlowLocator implements FlowLocator {
		private FlowLocator flowLocator;

		private Flow rootFlow;

		public FlowSessionFlowLocator(Flow rootFlow, FlowLocator flowLocator) {
			this.rootFlow = rootFlow;
			this.flowLocator = flowLocator;
		}

		public FlowDefinition getFlow(String id) throws FlowArtifactLookupException {
			if (rootFlow.getId().equals(id)) {
				return rootFlow;
			}
			else if (rootFlow.containsInlineFlow(id)) {
				return rootFlow.getInlineFlow(id);
			}
			else {
				return flowLocator.getFlow(id);
			}
		}
	}
}