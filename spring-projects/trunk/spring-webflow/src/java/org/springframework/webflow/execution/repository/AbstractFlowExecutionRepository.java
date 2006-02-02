package org.springframework.webflow.execution.repository;

import java.io.Serializable;

import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;

/**
 * A convenient base for flow execution repository implementations.
 * <p>
 * Exposes a configuration interface for setting the set of services common to
 * most repository implementations. Also provides some basic implementation
 * assistance.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionRepository implements FlowExecutionRepository {

	/**
	 * A holder for the services needed by this repository.
	 */
	private transient FlowExecutionRepositoryServices repositoryServices;

	/**
	 * No-arg constructor to satisfy use with implementations are that
	 * serializable.
	 */
	protected AbstractFlowExecutionRepository() {

	}

	/**
	 * Creates a new flow execution repository
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public AbstractFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		this.repositoryServices = repositoryServices;
	}

	/**
	 * Returns the holder for accessing common services needed by this
	 * repository.
	 */
	public FlowExecutionRepositoryServices getRepositoryServices() {
		return repositoryServices;
	}

	/**
	 * Sets the holder for accessing common services needed by this repository.
	 */
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

	protected FlowExecution rehydrate(FlowExecution flowExecution) {
		((FlowExecutionImpl)flowExecution).rehydrate(repositoryServices.getFlowLocator(), repositoryServices
				.getListenerLoader());
		return flowExecution;
	}

	/**
	 * Helper to generate a new unique object identifier using the configured
	 * {@link FlowExecutionRepositoryServices}.
	 * @return the generated uid
	 */
	protected Serializable generateId() {
		return repositoryServices.getUidGenerator().generateId();
	}
}