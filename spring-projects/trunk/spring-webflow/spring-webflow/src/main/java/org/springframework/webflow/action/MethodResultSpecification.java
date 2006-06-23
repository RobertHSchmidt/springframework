/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.action;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Specifies how a bean method result value should be exposed to an executing flow.
 * The return value is exposed as an attribute in a configured scope.
 * 
 * @see org.springframework.webflow.action.AbstractBeanInvokingAction
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Keith Donald
 */
public class MethodResultSpecification {

	/**
	 * The name of the attribute to index the return value of the invoked bean
	 * method.
	 */
	private String resultName;

	/**
	 * The scope of the attribute indexing the return value of the invoked bean
	 * method. Default is {@link ScopeType#REQUEST).
	 */
	private ScopeType resultScope;

	/**
	 * Creates a new bean method result specification.
	 * @param resultName the result name
	 * @param resultScope the result scope
	 */
	public MethodResultSpecification(String resultName, ScopeType resultScope) {
		Assert.notNull(resultName, "The result name is required");
		Assert.notNull(resultScope, "The result scope is required");
		this.resultName = resultName;
		this.resultScope = resultScope;
	}

	/**
	 * Returns name of the attribute to index the return value of the invoked
	 * bean method.
	 */
	public String getResultName() {
		return resultName;
	}

	/**
	 * Returns the scope the attribute indexing the return value of the invoked
	 * bean method.
	 */
	public ScopeType getResultScope() {
		return resultScope;
	}

	/**
	 * Expose given bean method return value in given flow execution request
	 * context.
	 * @param returnValue the return value
	 * @param context the request context
	 */
	public void exposeResult(Object returnValue, RequestContext context) {
		resultScope.getScope(context).put(resultName, returnValue);
	}

	public String toString() {
		return new ToStringCreator(this).append("resultName", resultName).append("resultScope", resultScope).toString();
	}
}