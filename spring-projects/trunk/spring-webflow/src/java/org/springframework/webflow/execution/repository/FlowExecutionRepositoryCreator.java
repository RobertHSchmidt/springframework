package org.springframework.webflow.execution.repository;

/**
 * A creational strategy that encapsulates the construction, configuration, and
 * rehydration of a flow execution repository implementation. An explicit
 * factory is used as a repository implementation can be a feature-rich object
 * with many tweakable settings, and may also be serialized out between
 * requests.
 * @author Keith Donald
 */
public interface FlowExecutionRepositoryCreator {

	/**
	 * Creates a new flow execution repository. The instance returned is always
	 * a prototype, a new instance is expeted to be created on each invocation.
	 * @return the fully constructed flow execution repository
	 */
	public FlowExecutionRepository createRepository();

	/**
	 * Rehydrate this flow execution repository, restoring any transient
	 * references that may be null as a result of the repository being
	 * deserialized. May not apply to all repository implementations.
	 * @param repository the potentially deserialized repository
	 * @return the rehydrated repository
	 */
	public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository);
}