package org.springframework.webflow.action.bean;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;

/**
 * A specification for a <code>Method</code>, consisting of the methodName
 * and an optional set of named arguments.
 * 
 * @author Keith Donald
 */
public class MethodKey implements Serializable {

	/**
	 * The name of the method, e.g execute
	 */
	private String methodName;

	/**
	 * The arguments of the method, e.g int arg1
	 */
	private Arguments arguments;

	/**
	 * Creates a method key with no arguments
	 * 
	 * @param methodName
	 *            the name of the method.
	 */
	public MethodKey(String methodName) {
		this(methodName, Arguments.NONE);
	}

	/**
	 * Creates a method key with a single argument.
	 * @param methodName the name of the method
	 * @param argument the method argument
	 */
	public MethodKey(String methodName, Argument argument) {
		this(methodName, new Arguments(argument));
	}

	/**
	 * Creates a method key with a list of arguments.
	 * @param methodName the name of the method
	 * @param arguments the method arguments
	 */
	public MethodKey(String methodName, Arguments arguments) {
		this.methodName = methodName;
		this.arguments = arguments;
	}

	public Arguments getArguments() {
		return arguments;
	}

	public String getMethodName() {
		return methodName;
	}

	/**
	 * Lookup the method for this key on the provided class.
	 * @param clazz the class
	 * @return the retrieved method
	 * @throws NoSuchMethodException no such method was found
	 */
	public Method lookupMethod(Class clazz) throws NoSuchMethodException {
		return clazz.getMethod(methodName, arguments.getTypesArray());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MethodKey)) {
			return false;
		}
		MethodKey other = (MethodKey) obj;
		return methodName.equals(methodName)
				&& arguments.equals(other.arguments);
	}

	public int hashCode() {
		return methodName.hashCode() + arguments.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("methodName", methodName)
				.append("arguments", arguments).toString();
	}
}