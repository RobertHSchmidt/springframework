package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.DelegatingFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.SharedMapFlowExecutionRepositoryFactory;

/**
 * A subclass of {@link SharedMapFlowExecutionRepositoryFactory} that simply
 * uses a
 * {@link ContinuationFlowExecutionRepositoryCreator continuation-based flow execution repository factory}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a server-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.executor.FlowExecutorImpl}.
 * 
 * @see ContinuationFlowExecutionRepositoryCreator
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	public ContinuationFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SharedMapFlowExecutionRepositoryFactory(
				new ContinuationFlowExecutionRepositoryCreator(this)));
	}

	protected ContinuationFlowExecutionRepositoryCreator getRepositoryCreator() {
		SharedMapFlowExecutionRepositoryFactory factory = (SharedMapFlowExecutionRepositoryFactory)getRepositoryFactory();
		return (ContinuationFlowExecutionRepositoryCreator)factory.getRepositoryCreator();
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		getRepositoryCreator().setContinuationFactory(continuationFactory);
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in this
	 * repository.
	 */
	public void setMaxContinuations(int maxContinuations) {
		getRepositoryCreator().setMaxContinuations(maxContinuations);
	}
}