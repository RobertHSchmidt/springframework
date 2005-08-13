/**
 * 
 */
package org.springframework.webflow.action.bean;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when a method key could not be resolved to an invokable java Method on
 * a Class.
 * 
 * @author Keith Donald
 */
public class InvalidMethodKeyException extends NestedRuntimeException {

	/**
	 * Creates an exception for the specified class method key with the
	 * specified root cause.
	 * 
	 * @param methodKey
	 *            the method key
	 * @param cause
	 *            the cause
	 */
	public InvalidMethodKeyException(ClassMethodKey methodKey, Exception cause) {
		super("Could not resolve method with key: " + methodKey, cause);
	}
}