package org.springframework.webflow.builder;

import java.io.Serializable;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.ScopeType;

public class MethodInfo implements Serializable {
	private MethodSignature method;

	private String resultName;

	private ScopeType resultScope;

	public MethodInfo(MethodSignature method) {
		this.method = method;
	}

	public MethodInfo(MethodSignature method, String resultName, ScopeType resultScope) {
		this.method = method;
		this.resultName = resultName;
		this.resultScope = resultScope;
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
}