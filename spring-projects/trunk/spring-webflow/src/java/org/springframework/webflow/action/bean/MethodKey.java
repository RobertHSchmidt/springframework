package org.springframework.webflow.action.bean;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;

public class MethodKey implements Serializable {
	private String methodName;

	private Arguments arguments;

	public MethodKey(String methodName) {
		this(methodName, Arguments.NONE);
	}

	public MethodKey(String methodName, Argument argument) {
		this(methodName, new Arguments(argument));
	}

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