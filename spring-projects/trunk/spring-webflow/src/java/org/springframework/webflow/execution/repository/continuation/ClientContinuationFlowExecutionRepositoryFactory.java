package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.repository.SingletonFlowExecutionRepositoryFactory;

/**
 * A subclass of {@link SingletonFlowExecutionRepositoryFactory} that simply
 * uses a
 * {@link ClientContinuationFlowExecutionRepository client continuation-based flow execution repository}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a client-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.manager.FlowExecutionManagerImpl}.
 * 
 * @see ClientContinuationFlowExecutionRepository
 * 
 * @author Keith Donald
 */
public class ClientContinuationFlowExecutionRepositoryFactory extends SingletonFlowExecutionRepositoryFactory {

	/**
	 * Creates a new client continuation flow execution repository factory.
	 */
	public ClientContinuationFlowExecutionRepositoryFactory() {
		super(new ClientContinuationFlowExecutionRepository());
	}
}