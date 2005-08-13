/**
 * 
 */
package org.springframework.webflow.action.bean;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;

public class ClassMethodKey implements Serializable {
	Class clazz;

	MethodKey methodKey;

	public ClassMethodKey(Class clazz, MethodKey methodKey) {
		this.clazz = clazz;
		this.methodKey = methodKey;
	}

	public Class getClazz() {
		return clazz;
	}

	public MethodKey getMethodKey() {
		return methodKey;
	}

	public Method lookupMethod() throws NoSuchMethodException {
		return methodKey.lookupMethod(clazz);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClassMethodKey)) {
			return false;
		}
		ClassMethodKey other = (ClassMethodKey) obj;
		return clazz.equals(other.clazz)
				&& methodKey.equals(other.methodKey);
	}

	public int hashCode() {
		return clazz.hashCode() + methodKey.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("class", clazz).append(
				"method", methodKey).toString();
	}
}