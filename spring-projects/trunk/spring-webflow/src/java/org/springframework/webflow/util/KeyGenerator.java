package org.springframework.webflow.util;

import java.io.Serializable;

/**
 * A strategy for generating keys for uniquely identifying execution artifacts
 * such as FlowExecutions, transaction tokens, and any other uniquely identified
 * flow artifact.
 * 
 * @author Keith Donald
 */
public interface KeyGenerator {

	/**
	 * Generate a new unique key.
	 * @return a serializable key, guaranteed to be unique
	 */
	public Serializable generate();
}
