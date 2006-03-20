package org.springframework.webflow.builder;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.ScopeType;

public class BeanInvokingActionParameters extends FlowArtifactParameters {
	private MethodInfo methodInfo;

	private ScopeType beanScope;

	public BeanInvokingActionParameters(String id, MethodInfo methodInfo, ScopeType beanScope,
			AttributeCollection customAttributes) {
		super(id, customAttributes);
		this.methodInfo = methodInfo;
		this.beanScope = beanScope;
	}

	public MethodSignature getMethod() {
		return methodInfo.getMethod();
	}

	public String getResultName() {
		return methodInfo.getResultName();
	}

	public ScopeType getResultScope() {
		return methodInfo.getResultScope();
	}

	public ScopeType getBeanScope() {
		return beanScope;
	}
}