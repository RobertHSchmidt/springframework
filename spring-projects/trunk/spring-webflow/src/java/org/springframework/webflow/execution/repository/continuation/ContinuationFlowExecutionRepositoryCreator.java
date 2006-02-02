package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.repository.AbstractFlowExecutionRepositoryCreator;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryServices;

/**
 * Creates continuation-based flow execution repositories.
 * <p>
 * All properties are optional. If a property is not set, the default values
 * will be used.
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepositoryCreator extends AbstractFlowExecutionRepositoryCreator {

	/**
	 * The flow execution continuation factory to use.
	 */
	private FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * The maximum number of continuations allowed per conversation.
	 */
	private int maxContinuations = 25;

	/**
	 * The flag indicating if this repository should turn on support for shared
	 * <i>conversational scope</i>.
	 * <p>
	 * Data stored in this scope is shared by all flow sessions in all
	 * continuations associated with an active conversation.
	 */
	private boolean enableConversationScope = true;
	
	/**
	 * Creates a new continuation repository creator.
	 * @param repositoryServices the repository services holder
	 */
	public ContinuationFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices);
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in repositories created by this creator.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in
	 * repositories created by this creator.
	 */
	public void setMaxContinuations(int maxContinuations) {
		this.maxContinuations = maxContinuations;
	}

	/**
	 * Returns the flag indicating if this repository has support for shared
	 * <i>conversational scope</i> enabled.
	 */
	public boolean isEnableConversationScope() {
		return enableConversationScope;
	}

	/**
	 * Sets the flag indicating if this repository should turn on support for
	 * shared <i>conversational scope</i>.
	 * <p>
	 * Data stored in this scope is <u>shared</u> by all flow sessions in all
	 * continuations associated with an active conversation.
	 */
	public void setEnableConversationScope(boolean enableConversationScope) {
		this.enableConversationScope = enableConversationScope;
	}
	
	public FlowExecutionRepository createRepository() {
		ContinuationFlowExecutionRepository repository = new ContinuationFlowExecutionRepository(
				getRepositoryServices());
		repository.setContinuationFactory(continuationFactory);
		repository.setMaxContinuations(maxContinuations);
		repository.setEnableConversationScope(enableConversationScope);
		return repository;
	}

	public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository) {
		ContinuationFlowExecutionRepository impl = (ContinuationFlowExecutionRepository)repository;
		impl.setRepositoryServices(getRepositoryServices());
		impl.setContinuationFactory(continuationFactory);
		return impl;
	}
}