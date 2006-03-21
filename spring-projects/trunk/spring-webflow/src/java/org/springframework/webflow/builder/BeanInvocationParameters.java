package org.springframework.webflow.builder;

import java.io.Serializable;

import org.springframework.binding.method.MethodSignature;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.AbstractBeanInvokingAction;

/**
 * Simple immutable parameter object (DTO) that holds configuration properties
 * that affect the behavior of a {@link AbstractBeanInvokingAction} method
 * invocation.
 * 
 * @author Keith Donald
 */
public class BeanInvocationParameters implements Serializable {

	/**
	 * The signature of the method to invoke on the bean.
	 */
	private MethodSignature method;

	/**
	 * The attribute name used to expose the method return value.
	 */
	private String resultName;

	/**
	 * The scope of the method return value attribute.
	 */
	private ScopeType resultScope;

	/**
	 * Creates a new parameter object.
	 * @param method the method signature
	 */
	public BeanInvocationParameters(MethodSignature method) {
		this.method = method;
	}

	/**
	 * Creates a new parameter object.
	 * @param method the method signature
	 * @param resultName the result name
	 * @param resultScope the result scope
	 */
	public BeanInvocationParameters(MethodSignature method, String resultName, ScopeType resultScope) {
		this.method = method;
		this.resultName = resultName;
		this.resultScope = resultScope;
	}

	/**
	 * Returns the signature of the method to invoke on the bean.
	 */
	public MethodSignature getMethod() {
		return method;
	}

	/**
	 * Returns the attribute name used to expose the method return value.
	 */
	public String getResultName() {
		return resultName;
	}

	/**
	 * Returns the scope of the method return value attribute.
	 */
	public ScopeType getResultScope() {
		return resultScope;
	}

	public String toString() {
		return new ToStringCreator(this).append("method", method).append("resultName", resultName).append(
				"resultScope", resultScope).toString();
	}
}