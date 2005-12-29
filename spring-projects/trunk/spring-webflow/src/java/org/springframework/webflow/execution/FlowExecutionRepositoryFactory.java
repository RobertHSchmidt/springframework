package org.springframework.webflow.execution;

/**
 * An abstract factory that encapsulates the construction and configuration of a
 * flow execution repository. An explicit factory is used as a repositry
 * implementation can be a feature-rich object with many tweakable settings.
 * @author Keith Donald
 */
public interface FlowExecutionRepositoryFactory {

	/**
	 * Creates a new flow execution repository. The instance returned is always
	 * a prototype, a new instance is expeted to be created on each invocation.
	 * @return the fully constructed flow execution repository
	 */
	public FlowExecutionRepository createRepository();
}