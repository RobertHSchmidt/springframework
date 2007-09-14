/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.faces.webflow.el;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Custom variable resolver that resolves to a thread-bound RequestContext object for binding expressions prefixed with
 * a {@link #REQUEST_CONTEXT_VARIABLE_NAME}. For instance "requestContext.conversationScope.myProperty".
 * 
 * This class is designed to be used with a {@link RequestContextPropertyResolver}.
 * 
 * @author Keith Donald
 * @author Jeremy Grelle
 */
public class RequestContextVariableResolver extends VariableResolver {

	/**
	 * Name of the request context variable.
	 */
	public static final String REQUEST_CONTEXT_VARIABLE_NAME = "requestContext";

	/**
	 * The standard variable resolver to delegate to if this one doesn't apply.
	 */
	private VariableResolver resolverDelegate;

	/**
	 * Creates a new request context variable resolver that resolves the current RequestContext object.
	 * @param resolverDelegate the resolver to delegate to when the variable is not named "requestContext".
	 */
	public RequestContextVariableResolver(VariableResolver resolverDelegate) {
		this.resolverDelegate = resolverDelegate;
	}

	/**
	 * Returns the variable resolver this resolver delegates to if necessary.
	 */
	protected final VariableResolver getResolverDelegate() {
		return resolverDelegate;
	}

	public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
		if (REQUEST_CONTEXT_VARIABLE_NAME.equals(name)) {
			return RequestContextHolder.getRequestContext();
		} else {
			return resolverDelegate.resolveVariable(context, name);
		}
	}
}