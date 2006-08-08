package org.springframework.webflow.engine;

import org.springframework.webflow.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.execution.factory.support.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.support.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.registry.FlowLocator;

public class FlowExecutionImplFactory implements FlowExecutionFactory {

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client.
	 */
	private FlowLocator flowLocator;

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private FlowExecutionListenerLoader listenerLoader = new StaticFlowExecutionListenerLoader();
	
	public FlowExecutionImplFactory(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}
		
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		this.listenerLoader = listenerLoader;
	}

	public FlowExecution createFlowExecution(String flowId) {
		FlowDefinition flow = flowLocator.getFlow(flowId);
		return new FlowExecutionImpl((Flow)flowLocator.getFlow(flowId), listenerLoader.getListeners(flow));
	}

	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope) {
		return flowExecution;
	}
}