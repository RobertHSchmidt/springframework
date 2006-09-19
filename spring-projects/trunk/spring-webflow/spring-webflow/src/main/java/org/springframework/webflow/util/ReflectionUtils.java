package org.springframework.webflow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Simple utility class for working with the java reflection API. Only intended for
 * internal use.  Will likely disappear in a future release of Spring Web Flow and
 * simply rely on {@link org.springframework.util.ReflectionUtils} if necessary.
 * 
 * @author Keith Donald
 */
public class ReflectionUtils {

	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with no arguments The target object can be <code>null</code> when
	 * invoking a static {@link Method}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, null);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with the supplied arguments The target object can be null when invoking a
	 * static {@link Method}.
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException(Exception)}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target, Object[] args) {
		try {
			return method.invoke(target, args);
		}
		catch (IllegalAccessException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
					+ ex.getMessage());
		}
		catch (InvocationTargetException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
					+ ex.getMessage());
		}
	}
	
	/**
	 * Handle the given reflection exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of an InvocationTargetException with such a root cause. Throws
	 * an IllegalStateException with an appropriate message else.
	 * @param ex the reflection exception to handle
	 */
	private static void handleReflectionException(Exception ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if (ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		throw new IllegalStateException(
				"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
	}
	
	/**
	 * Handle the given invocation target exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of such a root cause. Throws an IllegalStateException else.
	 * @param ex the invocation target exception to handle
	 */
	private static void handleInvocationTargetException(InvocationTargetException ex) {
		if (ex.getTargetException() instanceof RuntimeException) {
			throw (RuntimeException) ex.getTargetException();
		}
		if (ex.getTargetException() instanceof Error) {
			throw (Error) ex.getTargetException();
		}
		throw new IllegalStateException(
				"Unexpected exception thrown by method - " + ex.getTargetException().getClass().getName() +
				": " + ex.getTargetException().getMessage());
	}
}