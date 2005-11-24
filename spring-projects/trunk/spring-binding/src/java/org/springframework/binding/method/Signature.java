package org.springframework.binding.method;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

/**
 * A class method signature.
 * 
 * @author Keith Donald
 */
public class Signature implements Serializable {

	/**
	 * The class the method is a member of.
	 */
	private Class type;

	/**
	 * The name of the method.
	 */
	private String methodName;

	/**
	 * The method's parameter types.
	 */
	private Class[] parameterTypes;

	/**
	 * A cached handle to the resolved method (may be null).
	 */
	private transient Method method;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param name the name
	 */
	public Signature(Class type, String methodName, Class[] parameterTypes) {
		this.type = type;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
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

	public Method getMethod() throws InvalidMethodSignatureException {
		if (method == null) {
			method = resolveMethod();
		}
		return method;
	}

	protected Method resolveMethod() throws InvalidMethodSignatureException {
		try {
			return type.getMethod(getMethodName(), getParameterTypes());
		}
		catch (NoSuchMethodException e) {
			Method method = findMethodConsiderAssignableParameterTypes();
			if (method != null) {
				return method;
			}
			else {
				throw new InvalidMethodSignatureException(this, e);
			}
		}
	}

	protected Method findMethodConsiderAssignableParameterTypes() {
		Method[] candidateMethods = getType().getMethods();
		for (int i = 0; i < candidateMethods.length; i++) {
			if (candidateMethods[i].getName().equals(getMethodName())) {
				// Check if the method has the correct number of parameters.
				Class[] candidateParameterTypes = candidateMethods[i].getParameterTypes();
				if (candidateParameterTypes.length == getParameterTypes().length) {
					int numberOfCorrectArguments = 0;
					for (int j = 0; j < candidateParameterTypes.length; j++) {
						// Check if the candidate type is assignable to the sig
						// parameter type.
						if (BeanUtils.isAssignable(candidateParameterTypes[j], parameterTypes[j])) {
							numberOfCorrectArguments++;
						}
					}
					if (numberOfCorrectArguments == parameterTypes.length) {
						return candidateMethods[i];
					}
				}
			}
		}
		return null;
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
			if (!ObjectUtils.nullSafeEquals(parameterTypes[i], other[i])) {
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
			Class parameterType = parameterTypes[i];
			if (parameterType != null) {
				hash += parameterTypes[i].hashCode();
			}
		}
		return hash;
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("methodName", methodName).append("parameterTypes",
				parameterTypes).toString();
	}
}