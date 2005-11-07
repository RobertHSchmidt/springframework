package org.springframework.binding.method;

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
	 * The parameter types of the method, e.g int param1
	 */
	private Parameters parameters;

	/**
	 * Creates a method key with no arguments
	 * 
	 * @param methodName the name of the method.
	 */
	public MethodKey(String methodName) {
		this(methodName, Parameters.NONE);
	}

	/**
	 * Creates a method key with a single argument.
	 * @param methodName the name of the method
	 * @param parameter the method argument
	 */
	public MethodKey(String methodName, Parameter parameter) {
		this(methodName, new Parameters(parameter));
	}

	/**
	 * Creates a method key with a list of arguments.
	 * @param methodName the name of the method
	 * @param parameters the method arguments
	 */
	public MethodKey(String methodName, Parameters parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MethodKey)) {
			return false;
		}
		MethodKey other = (MethodKey)obj;
		return methodName.equals(methodName) && parameters.equals(other.parameters);
	}

	public int hashCode() {
		return methodName.hashCode() + parameters.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("methodName", methodName).append("parameters", parameters).toString();
	}
}