package org.springframework.webflow.config;

import org.springframework.webflow.Flow;

/**
 * A holder for accessing a reference to a Flow definition.
 * @author Keith Donald
 */
public interface FlowHolder {

	/**
	 * Returns the held flow definition.
	 */
	public Flow getFlow();
}
