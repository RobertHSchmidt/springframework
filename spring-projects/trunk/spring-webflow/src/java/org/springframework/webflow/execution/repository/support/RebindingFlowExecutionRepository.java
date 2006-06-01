package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.webflow.SharedMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.ConversationLock;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * A proxy that rebinds a target flow execution repository to its shared map on
 * change. This is used to support notifying shared map's that their underlying
 * entries changed, for purposes of clustering for example.
 * 
 * @author Keith Donald
 */
class RebindingFlowExecutionRepository implements FlowExecutionRepository {

	private FlowExecutionRepository target;

	private Object key;

	private SharedMap map;

	public RebindingFlowExecutionRepository(FlowExecutionRepository target, Object key, SharedMap map) {
		this.target = target;
		this.key = key;
		this.map = map;
	}

	public FlowExecution createFlowExecution(String flowId) throws FlowExecutionRepositoryException {
		return target.createFlowExecution(flowId);
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) throws FlowExecutionRepositoryException {
		return target.generateKey(flowExecution);
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution, Serializable conversationId)
			throws FlowExecutionRepositoryException {
		return target.generateKey(flowExecution, conversationId);
	}

	public FlowExecutionKey getCurrentFlowExecutionKey(Serializable conversationId)
			throws FlowExecutionRepositoryException {
		return target.getCurrentFlowExecutionKey(conversationId);
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return target.getFlowExecution(key);
	}

	public ConversationLock getLock(Serializable conversationId) throws FlowExecutionRepositoryException {
		return target.getLock(conversationId);
	}

	public void invalidateConversation(Serializable conversationId) throws FlowExecutionRepositoryException {
		target.invalidateConversation(conversationId);
		rebind();
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException {
		target.putFlowExecution(key, flowExecution);
		rebind();
	}

	private void rebind() {
		synchronized (map.getMutex()) {
			map.put(key, target);
		}
	}
}