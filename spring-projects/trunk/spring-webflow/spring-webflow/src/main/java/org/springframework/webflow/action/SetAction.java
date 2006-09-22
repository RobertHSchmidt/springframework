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

import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

/**
 * <p>
 * An action that sets an attribute in a {@link ScopeType scope} when executed.
 * </p>
 * 
 * @author Keith Donald
 */
public class SetAction extends AbstractAction {

	/**
	 * The property expression for setting the scoped attribute value.
	 */
	private PropertyExpression attributeExpression;

	/**
	 * The scope type.
	 */
	private ScopeType scope;

	/**
	 * The expression for resolving the scoped attribute value. 
	 */
	private Expression valueExpression;

	/**
	 * Creates a new set attribute action.
	 * @param attributeExpression the writeable attribute expression
	 * @param scope the scope the attribute is in
	 * @param valueExpression the evaluatable attribute value expression
	 */
	public SetAction(PropertyExpression attributeExpression, ScopeType scope, Expression valueExpression) {
		Assert.notNull(attributeExpression, "The attribute expression is required");
		Assert.notNull(scope, "The scope is required");
		Assert.notNull(valueExpression, "The value expression is required");
		this.attributeExpression = attributeExpression;
		this.scope = scope;
		this.valueExpression = valueExpression;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Map evaluationContext = getEvaluationContext(context);
		Object value = valueExpression.evaluateAgainst(context, evaluationContext);
		MutableAttributeMap scopeMap = scope.getScope(context);
		attributeExpression.setValue(scopeMap, value, evaluationContext);
		return success();
	}

	/**
	 * Template method subclasses may override to customize the expressin
	 * evaluation context.
	 * @param context the request context
	 * @return the evaluation context
	 */
	protected Map getEvaluationContext(RequestContext context) {
		return null;
	}
}