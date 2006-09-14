package org.springframework.webflow.engine.impl;

import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;

/**
 * Restores the transient state of deserialized {@link FlowExecutionImpl}
 * objects.
 * 
 * @author Keith Donald
 */
public class FlowExecutionImplStateRestorer implements FlowExecutionStateRestorer {

	/**
	 * Used to restore the flow execution's flow definition.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * Used to restore the flow execution's listeners.
	 */
	private FlowExecutionListenerLoader executionListenerLoader = StaticFlowExecutionListenerLoader.EMPTY_INSTANCE;

	/**
	 * Used to restore the flow execution's system attributes.
	 */
	private AttributeMap executionAttributes = CollectionUtils.EMPTY_ATTRIBUTE_MAP;

	/**
	 * Creates a new execution transient state restorer.
	 * @param definitionLocator the flow definition locator
	 */
	public FlowExecutionImplStateRestorer(FlowDefinitionLocator definitionLocator) {
		Assert.notNull(definitionLocator, "The flow definition locator is required");
		this.definitionLocator = definitionLocator;
	}

	/**
	 * Sets the attributes to apply to flow executions created by this factory.
	 * Execution attributes may affect flow execution behavior.
	 * @param executionAttributes flow execution system attributes
	 */
	public void setExecutionAttributes(AttributeMap executionAttributes) {
		Assert.notNull(executionAttributes, "The execution attributes map is required");
		this.executionAttributes = executionAttributes;
	}

	/**
	 * Sets the strategy for loading listeners that should observe executions of
	 * a flow definition. Allows full control over what listeners should apply
	 * for executions of a flow definition.
	 */
	public void setExecutionListenerLoader(FlowExecutionListenerLoader executionListenerLoader) {
		Assert.notNull(executionListenerLoader, "The listener loader is required");
		this.executionListenerLoader = executionListenerLoader;
	}

	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope) {
		FlowExecutionImpl impl = (FlowExecutionImpl)flowExecution;
		if (impl.isStateRestored()) {
			return impl;
		}
		Flow flow = (Flow)definitionLocator.getFlowDefinition(impl.flowId);
		impl.flow = flow;
		Iterator it = impl.flowSessions.iterator();
		FlowSessionFlowDefinitionLocator locator = new FlowSessionFlowDefinitionLocator(flow, definitionLocator);
		while (it.hasNext()) {
			FlowSessionImpl session = (FlowSessionImpl)it.next();
			session.flow = (Flow)locator.getFlowDefinition(session.flowId);
			session.state = session.flow.getStateInternal(session.stateId);
		}
		if (conversationScope == null) {
			conversationScope = new LocalAttributeMap();
		}
		impl.conversationScope = conversationScope;
		impl.listeners = new FlowExecutionListeners(executionListenerLoader.getListeners(flow));
		impl.attributes = executionAttributes;
		return flowExecution;
	}

	private static class FlowSessionFlowDefinitionLocator implements FlowDefinitionLocator {
		private FlowDefinitionLocator flowLocator;

		private Flow rootFlow;

		public FlowSessionFlowDefinitionLocator(Flow rootFlow, FlowDefinitionLocator flowLocator) {
			this.rootFlow = rootFlow;
			this.flowLocator = flowLocator;
		}

		public FlowDefinition getFlowDefinition(String id) {
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