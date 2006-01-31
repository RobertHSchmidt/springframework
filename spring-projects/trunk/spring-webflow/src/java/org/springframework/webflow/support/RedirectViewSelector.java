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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * View selector that makes selections that trigger a client side redirect. Only
 * parameter values encoded in the view (e.g.
 * "/viewName?param0=value0&param1=value1") are exposed.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class RedirectViewSelector implements ViewSelector, Serializable {

	/**
	 * The parsed, evaluatable redirect expression.
	 */
	private Expression expression;

	/**
	 * Create a new redirecting view descriptor creator that takes given
	 * expression as input. The expression is the parsed form
	 * (expression-tokenized) of the encoded view (e.g.
	 * "/viewName?param0=value0&param1=value1").
	 */
	public RedirectViewSelector(Expression expression) {
		this.expression = expression;
	}

	/**
	 * Returns the expression used by this view descriptor creator.
	 */
	protected Expression getExpression() {
		return expression;
	}

	public ViewSelection makeSelection(RequestContext context) {
		String fullView = (String)expression.evaluateAgainst(context, getEvaluationContext(context));
		// the resulting fullView should look something like
		// "/viewName?param0=value0&param1=value1"
		// now parse that and build a corresponding view descriptor
		int index = fullView.indexOf('?');
		String viewName;
		Map model = null;
		if (index != -1) {
			viewName = fullView.substring(0, index);
			String[] parameters = StringUtils.delimitedListToStringArray(fullView.substring(index + 1), "&");
			model = new HashMap(parameters.length, 1);
			for (int i = 0; i < parameters.length; i++) {
				String nameAndValue = parameters[i];
				index = nameAndValue.indexOf('=');
				if (index != -1) {
					model.put(nameAndValue.substring(0, index), nameAndValue.substring(index + 1));
				}
				else {
					model.put(nameAndValue, "");
				}
			}
		} else {
			viewName = fullView;
		}
		return new ViewSelection(viewName, model, true);
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