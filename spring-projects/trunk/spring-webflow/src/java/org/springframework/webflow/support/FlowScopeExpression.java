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

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.support.Assert;
import org.springframework.webflow.RequestContext;

/**
 * Expression evaluator that evaluates an expression in flow scope.
 * 
 * @author Keith Donald
 */
public class FlowScopeExpression implements Expression {
	
	private Expression expression;
	
	/**
	 * Create a new expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param evaluator the nested evaluator to execute
	 */
	public FlowScopeExpression(Expression evaluator) {
		this.expression = evaluator;
	}
	
	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		Assert.isInstanceOf(RequestContext.class, target,
				"In the web flow system all expression evaluation is against the request context");
		RequestContext requestContext = (RequestContext)target;
		return expression.evaluateAgainst(requestContext.getFlowScope(), context);
	}
}