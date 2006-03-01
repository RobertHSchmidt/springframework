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
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Makes view selections requesting a client side redirect to an <i>external</i>
 * URL outside of the flow.
 * <p>
 * This selector is applicable when you wish to request a <i>redirect after
 * conversation completion</i> as part of entering an EndState. This selector
 * generally does not make sense to be used with a ViewState. If you seek
 * "in-flow" redirect behavior use a {@link SimpleViewSelector} instead with the
 * {@link SimpleViewSelector#isRequestConversationRedirect()} flag set to true.
 * <p>
 * Only parameter values encoded in the view (e.g.
 * "/viewName?param0=value0&param1=value1") are exposed in the model.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ExternalRedirectSelector implements ViewSelector, Serializable {

	/**
	 * The parsed, evaluatable redirect expression.
	 */
	private Expression urlExpression;

	/**
	 * Create a new redirecting view descriptor creator that takes given
	 * expression as input. The expression is the parsed form
	 * (expression-tokenized) of the encoded view (e.g.
	 * "/pathInfo?param0=value0&param1=value1").
	 */
	public ExternalRedirectSelector(Expression urlExpression) {
		this.urlExpression = urlExpression;
	}

	/**
	 * Returns the expression used by this view selector.
	 */
	protected Expression getUrlExpression() {
		return urlExpression;
	}

	public ViewSelection makeSelection(RequestContext context) {
		String url = (String)urlExpression.evaluateAgainst(context, getEvaluationContext(context));
		return new ExternalRedirect(url);
	}

	/**
	 * Setup the expression evaluation context.
	 */
	protected Map getEvaluationContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}

	public String toString() {
		return new ToStringCreator(this).append("urlExpression", urlExpression).toString();
	}
}