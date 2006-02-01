package org.springframework.webflow.execution.repository;

import java.io.Serializable;

import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;

/**
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepository implements FlowExecutionRepository {

	private transient FlowExecutionRepositoryServices repositoryServices;

	public AbstractFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		this.repositoryServices = repositoryServices;
	}

	public FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	public void setRepositoryServices(FlowExecutionRepositoryServices repositoryServices) {
		this.repositoryServices = repositoryServices;
	}

	public FlowExecution createFlowExecution(String flowId) {
		Flow flow = repositoryServices.getFlowLocator().getFlow(flowId);
		return new FlowExecutionImpl(flow, repositoryServices.getListenerLoader().getListeners(flow));
	}

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution) {
		return new FlowExecutionContinuationKey(generateId(), generateId());
	}

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution, Serializable conversationId) {
		return new FlowExecutionContinuationKey(conversationId, generateId());
	}

	protected Serializable generateId() {
		return repositoryServices.getUidGenerator().generateId();
	}

	protected FlowExecution rehydrate(FlowExecution flowExecution) {
		flowExecution.rehydrate(repositoryServices.getFlowLocator(), repositoryServices.getListenerLoader());
		return flowExecution;
	}	
}