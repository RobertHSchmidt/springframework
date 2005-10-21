package org.springframework.webflow.config;

import org.springframework.webflow.Flow;

/**
 * A holder for accessing a reference to a Flow definition. Provides a layer of
 * indirection for managing a loaded Flow definition.
 * @author Keith Donald
 */
public interface FlowHolder {

	/**
	 * Returns the Flow definition held by this holder.
	 */
	public Flow getFlow();
}
