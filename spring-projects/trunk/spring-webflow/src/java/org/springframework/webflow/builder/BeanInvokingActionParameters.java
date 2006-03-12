package org.springframework.webflow.builder;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.ScopeType;

public class BeanInvokingActionParameters extends FlowArtifactParameters {
	private boolean stateful;

	private MethodSignature method;

	private String resultName;

	private ScopeType resultScope;

	private ScopeType beanScope;
	
	public BeanInvokingActionParameters(String id, boolean stateful, MethodSignature method, String resultName,
			ScopeType resultScope, ScopeType beanScope, AttributeCollection customAttributes) {
		super(id, customAttributes);
		this.stateful = stateful;
		this.method = method;
		this.resultName = resultName;
		this.resultScope = resultScope;
		this.beanScope = beanScope;
	}

	public boolean isStateful() {
		return stateful;
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