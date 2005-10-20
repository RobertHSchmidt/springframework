package org.springframework.webflow.execution;

import java.io.Serializable;

/**
 * A strategy for generating keys for uniquely identifying execution artifacts
 * such as FlowExecutions and transaction tokens.
 * 
 * @author Keith Donald
 */
public interface KeyGenerator {

	/**
	 * Generate a new, unique key.
	 * @return a serializable key, guaranteed to be unique
	 */
	public Serializable generate();
}
