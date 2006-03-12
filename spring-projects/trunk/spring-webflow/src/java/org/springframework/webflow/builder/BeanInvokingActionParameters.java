package org.springframework.webflow.builder;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.ScopeType;

public class BeanInvokingActionParameters extends FlowArtifactParameters {
	private MethodSignature method;

	private String resultName;

	private ScopeType resultScope;

	private ScopeType beanScope;
	
	public BeanInvokingActionParameters(String id, MethodSignature method, String resultName,
			ScopeType resultScope, ScopeType beanScope, AttributeCollection customAttributes) {
		super(id, customAttributes);
		this.method = method;
		this.resultName = resultName;
		this.resultScope = resultScope;
		this.beanScope = beanScope;
	}

	public MethodSignature getMethod() {
		return method;
	}

	public String getResultName() {
		return resultName;
	}

	public ScopeType getResultScope() {
		return resultScope;
	}

	public ScopeType getBeanScope() {
		return beanScope;
	}	
}