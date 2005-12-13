package org.springframework.binding.method;

import java.lang.reflect.InvocationTargetException;

import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.StylerUtils;

/**
 * Base class for exceptions that report a method invocation failure.
 * @author Keith Donald
 */
public class MethodInvocationException extends NestedRuntimeException {
	private Signature signature;

	private Object[] arguments;

	/**
	 * Signals that the method with the specified signature could not be invoked
	 * with the provided arguments.
	 * @param signature the method sig
	 * @param arguments the arguments
	 * @param cause the root cause
	 */
	public MethodInvocationException(Signature signature, Object[] arguments, Exception cause) {
		super(
				"Unable to invoke method of signature [" + signature + "] with arguments "
						+ StylerUtils.style(arguments), cause);
	}

	/**
	 * Returns the invoked method's signature.
	 */
	public Signature getSignature() {
		return signature;
	}

	/**
	 * Returns the method invocation arguments;
	 */
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * Returns the target root cause exception of the method invocation failure.
	 * @returns the target throwable
	 */
	public Throwable getTargetException() {
		if (getCause() instanceof InvocationTargetException) {
			return ((InvocationTargetException)getCause()).getTargetException();
		}
		else {
			return getCause();
		}
	}
}