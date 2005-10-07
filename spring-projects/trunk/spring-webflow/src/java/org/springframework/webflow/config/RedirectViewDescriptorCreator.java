/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewDescriptorCreator;

/**
 * View descriptor creator that creates view descriptors requesting a
 * client side redirect. Only parameter values encoded in the view (e.g.
 * "/viewName?param0=value0&param1=value1") are exposed.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class RedirectViewDescriptorCreator implements ViewDescriptorCreator, Serializable {
	
	/**
	 * The parsed, evaluatable redirect expression.
	 */
	private Expression expression;
	
	/**
	 * Create a new redirecting view descriptor creator that takes given
	 * expression as input. The expression is the parsed form (expression-tokenized)
	 * of the encoded view (e.g. "/viewName?param0=value0&param1=value1").
	 */
	public RedirectViewDescriptorCreator(Expression expression) {
		this.expression = expression;
	}

	/**
	 * Returns the expression used by this view descriptor creator.
	 */
	protected Expression getExpression() {
		return expression;
	}
	
	public ViewDescriptor createViewDescriptor(RequestContext context) {
		ViewDescriptor viewDescriptor = new ViewDescriptor();
		viewDescriptor.setRedirect(true);
		String fullView = (String)expression.evaluateAgainst(context, getEvaluationContext(context));
		// the resulting fullView should look something like "/viewName?param0=value0&param1=value1"
		// now parse that and build a corresponding view descriptor
		int idx = fullView.indexOf('?');
		if (idx != -1) {
			viewDescriptor.setViewName(fullView.substring(0, idx));
			StringTokenizer parameters = new StringTokenizer(fullView.substring(idx + 1), "&");
			while (parameters.hasMoreTokens()) {
				String nameAndValue = parameters.nextToken();
				idx = nameAndValue.indexOf('=');
				if (idx !=-1) {
					viewDescriptor.addObject(nameAndValue.substring(0, idx), nameAndValue.substring(idx + 1));
				}
				else {
					viewDescriptor.addObject(nameAndValue, "");
				}
			}
		}
		else {
			// only view name specified (e.g. "/viewName")
			viewDescriptor.setViewName(fullView);
		}
		return viewDescriptor;
	}

	/**
	 * Setup the expression evaluation context.
	 */
	protected Map getEvaluationContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}
	
	public String toString() {
		return new ToStringCreator(this).append("expression", expression).toString();
	}
}