/**
 * 
 */
package org.springframework.binding.method;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;

/**
 * A method specifiation for a single type (class or interface) method: consists
 * of the class itself plus the method key, containing the method name and all
 * named arguments.
 * 
 * @author Keith Donald
 */
public class TypeMethodKey implements Serializable {

	/**
	 * The type - either a java class or interface.
	 */
	private Class type;

	/**
	 * The specification for the method on the type.
	 */
	private MethodKey methodKey;

	/**
	 * A method specified for a type.
	 * 
	 * @param type
	 *            the type
	 * @param methodKey
	 *            the method
	 */
	public TypeMethodKey(Class type, MethodKey methodKey) {
		this.type = type;
		this.methodKey = methodKey;
	}

	public Class getType() {
		return type;
	}

	public MethodKey getMethodKey() {
		return methodKey;
	}

	public Method lookupMethod() throws NoSuchMethodException {
		return methodKey.lookupMethod(type);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TypeMethodKey)) {
			return false;
		}
		TypeMethodKey other = (TypeMethodKey) obj;
		return type.equals(other.type) && methodKey.equals(other.methodKey);
	}

	public int hashCode() {
		return type.hashCode() + methodKey.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("class", type).append("method",
				methodKey).toString();
	}
}