package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.FlowExecutionRepository;
import org.springframework.webflow.execution.FlowExecutionRepositoryCreator;
import org.springframework.webflow.util.UidGenerator;

/**
 * A factory for creating continuation-based flow execution repositories.
 * <p>
 * All properties are optional. If a property is not set, the default value set
 * within {@link ContinuationFlowExecutionRepository} be used.
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepositoryCreator implements FlowExecutionRepositoryCreator {

	/**
	 * The flow execution continuation factory to use.
	 */
	private FlowExecutionContinuationFactory continuationFactory;

	/**
	 * The uid generation strategy to use.
	 */
	private UidGenerator uidGenerator;

	/**
	 * The maximum number of continuations allowed per conversation.
	 */
	private int maxContinuations;

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in repositories created by this creator.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	/**
	 * Sets the uid generator that generates unique identifiers for entries
	 * placed into repositories created by this creator.
	 */
	public void setUidGenerator(UidGenerator uidGenerator) {
		this.uidGenerator = uidGenerator;
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in
	 * repositories created by this creator.
	 */
	public void setMaxContinuations(int maxContinuations) {
		this.maxContinuations = maxContinuations;
	}

	public FlowExecutionRepository createRepository() {
		ContinuationFlowExecutionRepository repository = new ContinuationFlowExecutionRepository();
		if (continuationFactory != null) {
			repository.setContinuationFactory(continuationFactory);
		}
		if (uidGenerator != null) {
			repository.setUidGenerator(uidGenerator);
		}
		if (maxContinuations > 0) {
			repository.setMaxContinuations(maxContinuations);
		}
		return repository;
	}
}