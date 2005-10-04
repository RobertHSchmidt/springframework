package org.springframework.binding.method;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;

/**
 * A method signature.
 * 
 * @author Keith
 */
public class Signature implements Serializable {

	private Class type;

	private String methodName;

	private Class[] parameterTypes;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param name the name
	 */
	public Signature(Class type, String methodName, Class[] argumentTypes) {
		this.type = type;
		this.methodName = methodName;
		this.parameterTypes = argumentTypes;
	}

	public Class getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public Method lookupMethod() throws NoSuchMethodException {
		return type.getMethod(getMethodName(), getParameterTypes());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Signature)) {
			return false;
		}
		Signature other = (Signature)obj;
		return type.equals(other.type) && methodName.equals(other.methodName)
				&& argumentTypesEqual(other.parameterTypes);
	}

	private boolean argumentTypesEqual(Class[] other) {
		if (parameterTypes == other) {
			return true;
		}
		if (parameterTypes.length != other.length) {
			return false;
		}
		for (int i = 0; i < this.parameterTypes.length; i++) {
			if (!parameterTypes[i].equals(other[i])) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		return type.hashCode() + methodName.hashCode() + argumentTypesHash();
	}

	private int argumentTypesHash() {
		if (parameterTypes == null) {
			return 0;
		}
		int hash = 0;
		for (int i = 0; i < parameterTypes.length; i++) {
			hash += parameterTypes[i].hashCode();
		}
		return hash;
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("methodName", methodName).append("parameterTypes",
				parameterTypes).toString();
	}
}