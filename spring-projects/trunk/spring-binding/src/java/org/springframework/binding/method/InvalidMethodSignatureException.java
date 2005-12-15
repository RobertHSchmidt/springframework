/**
 * 
 */
package org.springframework.binding.method;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when a method key could not be resolved to an invokable java Method on
 * a Class.
 * 
 * @author Keith Donald
 */
public class InvalidMethodSignatureException extends NestedRuntimeException {

	/**
	 * Creates an exception for the specified class method key with the
	 * specified root cause.
	 * 
	 * @param methodKey the method key
	 * @param cause the cause
	 */
	public InvalidMethodSignatureException(Signature signature, Exception cause) {
		super("Could not resolve method with signature " + signature, cause);
	}
}