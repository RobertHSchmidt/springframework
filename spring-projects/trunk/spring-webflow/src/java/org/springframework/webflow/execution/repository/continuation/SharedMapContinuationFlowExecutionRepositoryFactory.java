package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.repository.SharedMapFlowExecutionRepositoryFactory;

/**
 * A subclass of {@link SharedMapFlowExecutionRepositoryFactory} that simply
 * uses a
 * {@link ContinuationFlowExecutionRepositoryCreator continuation-based flow execution repository factory}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a server-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.manager.FlowExecutionManagerImpl}.
 * 
 * @see ContinuationFlowExecutionRepositoryCreator
 * 
 * @author Keith Donald
 */
public class SharedMapContinuationFlowExecutionRepositoryFactory extends SharedMapFlowExecutionRepositoryFactory {
	public SharedMapContinuationFlowExecutionRepositoryFactory() {
		setRepositoryCreator(new ContinuationFlowExecutionRepositoryCreator());
	}
}