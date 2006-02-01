package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.DelegatingFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.SingletonFlowExecutionRepositoryFactory;

/**
 * A subclass of {@link SingletonFlowExecutionRepositoryFactory} that simply
 * uses a
 * {@link ClientContinuationFlowExecutionRepository client continuation-based flow execution repository}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a client-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.executor.FlowExecutorImpl}.
 * 
 * @see ClientContinuationFlowExecutionRepository
 * 
 * @author Keith Donald
 */
public class ClientContinuationFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {
	public ClientContinuationFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SingletonFlowExecutionRepositoryFactory(new ClientContinuationFlowExecutionRepository(
				this)));
	}
}