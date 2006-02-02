package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.DelegatingFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryServices;
import org.springframework.webflow.execution.repository.SingletonFlowExecutionRepositoryFactory;

/**
 * This is a convenient implementation that encapsulates the assembly of a
 * "client" flow execution repository factory and delegates to it at runtme. The
 * delegate factory creates repositories that persist flow executions
 * client-side, requiring no server-side state.
 * <p>
 * Internally, sets a {@link SingletonFlowExecutionRepositoryFactory} configured
 * with a single, stateless
 * {@link ClientContinuationFlowExecutionRepository client continuation-based flow execution repository}
 * implementation.
 * <p>
 * This class inherits from {@link FlowExecutionRepositoryServices} to allow for
 * direct configuration of services needed by the repositories created by this
 * factory.
 * 
 * @see ClientContinuationFlowExecutionRepository
 * 
 * @author Keith Donald
 */
public class ClientContinuationFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	/**
	 * Creates a new client flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
	 */
	public ClientContinuationFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SingletonFlowExecutionRepositoryFactory(new ClientContinuationFlowExecutionRepository(
				this)));
	}
}