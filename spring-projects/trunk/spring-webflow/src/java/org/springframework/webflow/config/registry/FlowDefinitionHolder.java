package org.springframework.webflow.config.registry;

import org.springframework.webflow.Flow;

/**
 * A holder holding a reference to a Flow definition. Provides a layer of
 * indirection for managing a refreshable Flow definition.
 * @author Keith Donald
 */
public interface FlowDefinitionHolder {

	/**
	 * Returns the id of the flow definition held by this holder. This is a
	 * "lightweight" method callers may call to obtain the id of the Flow
	 * without triggering further Flow creation.
	 */
	public String getId();

	/**
	 * Returns the Flow definition held by this holder.
	 */
	public Flow getFlow();

	/**
	 * Refresh the Flow definition held by this holder.
	 */
	public void refresh();
}
