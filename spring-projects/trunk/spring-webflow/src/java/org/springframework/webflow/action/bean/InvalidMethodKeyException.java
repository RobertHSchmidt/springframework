/**
 * 
 */
package org.springframework.webflow.action.bean;

import org.springframework.core.NestedRuntimeException;

public class InvalidMethodKeyException extends
		NestedRuntimeException {
	public InvalidMethodKeyException(ClassMethodKey methodKey,
			Exception cause) {
		super("Could not resolve method with key: " + methodKey, cause);
	}
}