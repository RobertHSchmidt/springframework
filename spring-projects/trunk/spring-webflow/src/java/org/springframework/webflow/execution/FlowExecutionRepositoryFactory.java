package org.springframework.webflow.execution;

import org.springframework.webflow.ExternalContext;

/**
 * An abstract factory for obtaining a reference to a flow execution repository
 * that may be managed in an external data structure.
 * @author Keith Donald
 */
public interface FlowExecutionRepositoryFactory {

	/**
	 * Lookup the repository given the external context.
	 * @param context the external context, which may be used to access the
	 * repository from an externally managed in-memory map
	 * @return the retrived flow execution repository
	 */
	public FlowExecutionRepository getRepository(ExternalContext context);
}