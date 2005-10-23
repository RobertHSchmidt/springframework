package org.springframework.webflow.config.registry;

import org.springframework.webflow.Flow;

/**
 * A holder holding a reference to a Flow definition. Provides a layer of
 * indirection for managing a refreshable Flow definition.
 * @author Keith Donald
 */
public interface FlowDefinitionHolder {

	/**
	 * Returns the <code>id</code> of the flow definition held by this holder.
	 * This is a <i>lightweight</i> method callers may call to obtain the id of
	 * the Flow without triggering full Flow definition assembly (which may be
	 * an expensive operation).
	 */
	public String getId();

	/**
	 * Returns the Flow definition held by this holder. Calling this method the
	 * first time may trigger Flow assembly.
	 */
	public Flow getFlow();

	/**
	 * Refresh the Flow definition held by this holder. Calling this method
	 * typically triggers Flow reassembly, which may include a refresh from an
	 * externalized resource such as a file.
	 */
	public void refresh();
}
