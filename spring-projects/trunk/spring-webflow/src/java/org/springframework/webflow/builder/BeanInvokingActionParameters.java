package org.springframework.webflow.builder;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.AbstractBeanInvokingAction;

/**
 * Extension of {@link FlowArtifactParameters} to support initializing the configuration of a
 * {@link AbstractBeanInvokingAction}. Used only during flow construction/configuration time.
 * 
 * @author Keith Donald
 */
public class BeanInvokingActionParameters extends FlowArtifactParameters {
	
	/**
	 * Instructions about what bean method to invoke. 
	 */
	private BeanInvocationParameters methodParameters;

	/**
	 * The scope to expose the invoked bean in (optional). 
	 */
	private ScopeType beanScope;

	/**
	 * Creates a new parameter object.
	 * @param id the action id
	 * @param methodParameters the method parameters
	 * @param beanScope the bean scope
	 * @param customAttributes custom initialization attributes
	 */
	public BeanInvokingActionParameters(String id, BeanInvocationParameters methodParameters,
			ScopeType beanScope, AttributeCollection customAttributes) {
		super(id, customAttributes);
		this.methodParameters = methodParameters;
		this.beanScope = beanScope;
	}

	/**
	 * Returns the signature of the method to invoke on the bean.
	 */
	public MethodSignature getMethod() {
		return methodParameters.getMethod();
	}

	/**
	 * Returns the attribute name used to expose the method return value.
	 */
	public String getResultName() {
		return methodParameters.getResultName();
	}

	/**
	 * Returns the scope of the method return value attribute.
	 */
	public ScopeType getResultScope() {
		return methodParameters.getResultScope();
	}

	/**
	 * Returns the scope to expose the invoked bean in. 
	 */
	public ScopeType getBeanScope() {
		return beanScope;
	}
}