package org.springframework.webflow.engine.builder.xml;

import org.springframework.binding.expression.EvaluationContext;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.SettableExpression;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

class ScopeExpression implements SettableExpression {

	private String attributeName;
	
	private ScopeType scopeType;
	
	public ScopeExpression(String attributeName, ScopeType scopeType) {
		this.attributeName = attributeName;
		this.scopeType = scopeType;
	}

	public Object evaluate(Object target, EvaluationContext context) throws EvaluationException {
		RequestContext requestContext = (RequestContext)target;
		return scopeType.getScope(requestContext).get(attributeName);
	}

	public void evaluateToSet(Object target, Object value, EvaluationContext context) throws EvaluationException {
		RequestContext requestContext = (RequestContext)target;
		scopeType.getScope(requestContext).put(attributeName, value);
	}
}
