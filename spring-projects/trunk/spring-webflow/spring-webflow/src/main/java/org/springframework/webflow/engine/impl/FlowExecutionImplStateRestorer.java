package org.springframework.webflow.engine.impl;

import java.util.Iterator;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowArtifactLookupException;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.support.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;

public class FlowExecutionImplStateRestorer implements FlowExecutionStateRestorer {

	private FlowDefinitionLocator flowLocator;

	private FlowExecutionListenerLoader listenerLoader;

	public FlowExecutionImplStateRestorer(FlowDefinitionLocator flowLocator, FlowExecutionListenerLoader listenerLoader) {
		this.flowLocator = flowLocator;
		this.listenerLoader = listenerLoader;
	}

	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope) {
		FlowExecutionImpl impl = (FlowExecutionImpl)flowExecution;
		if (impl.isStateRestored()) {
			return impl;
		}
		Flow flow = (Flow)flowLocator.getFlowDefinition(impl.flowId);
		Iterator it = impl.flowSessions.iterator();
		FlowSessionFlowDefinitionLocator locator = new FlowSessionFlowDefinitionLocator(flow, flowLocator);
		while (it.hasNext()) {
			FlowSessionImpl session = (FlowSessionImpl)it.next();
			session.flow = (Flow)locator.getFlowDefinition(session.flowId);
			session.state = flow.getStateInternal(session.stateId);
		}
		impl.listeners = new FlowExecutionListeners(listenerLoader.getListeners(flow));
		impl.conversationScope = conversationScope;
		return flowExecution;
	}

	private static class FlowSessionFlowDefinitionLocator implements FlowDefinitionLocator {
		private FlowDefinitionLocator flowLocator;

		private Flow rootFlow;

		public FlowSessionFlowDefinitionLocator(Flow rootFlow, FlowDefinitionLocator flowLocator) {
			this.rootFlow = rootFlow;
			this.flowLocator = flowLocator;
		}

		public FlowDefinition getFlowDefinition(String id) throws FlowArtifactLookupException {
			if (rootFlow.getId().equals(id)) {
				return rootFlow;
			}
			else if (rootFlow.containsInlineFlow(id)) {
				return rootFlow.getInlineFlow(id);
			}
			else {
				return flowLocator.getFlowDefinition(id);
			}
		}
	}
}