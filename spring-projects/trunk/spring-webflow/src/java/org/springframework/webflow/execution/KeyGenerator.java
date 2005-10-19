package org.springframework.webflow.execution;

import java.io.Serializable;

/**
 * @author Keith Donald
 */
public interface KeyGenerator {
	
	/**
	 * @return
	 */
	public Serializable generate();
}
