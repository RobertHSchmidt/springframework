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

	private Class[] argumentTypes;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param name the name
	 */
	public Signature(Class type, String methodName, Class[] argumentTypes) {
		this.type = type;
		this.methodName = methodName;
		this.argumentTypes = argumentTypes;
	}

	public Class getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class[] getArgumentTypes() {
		return argumentTypes;
	}

	public Method lookupMethod() throws NoSuchMethodException {
		return type.getMethod(getMethodName(), getArgumentTypes());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Signature)) {
			return false;
		}
		Signature other = (Signature)obj;
		return type.equals(other.type) && methodName.equals(other.methodName)
				&& argumentTypesEqual(other.argumentTypes);
	}

	private boolean argumentTypesEqual(Class[] other) {
		if (argumentTypes == other) {
			return true;
		}
		if (argumentTypes.length != other.length) {
			return false;
		}
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (!argumentTypes[i].equals(other[i])) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		return type.hashCode() + methodName.hashCode() + argumentTypesHash();
	}

	private int argumentTypesHash() {
		if (argumentTypes == null) {
			return 0;
		}
		int hash = 0;
		for (int i = 0; i < argumentTypes.length; i++) {
			hash += argumentTypes[i].hashCode();
		}
		return hash;
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("methodName", methodName).append("argumentTypes",
				argumentTypes).toString();
	}
}