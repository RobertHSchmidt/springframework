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
package org.springframework.binding.expression.support;

import java.util.Collections;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.binding.expression.EvaluationAttempt;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.expression.SetPropertyAttempt;
import org.springframework.util.Assert;

/**
 * Evaluates a parsed ognl expression.
 * <p>
 * IMPLEMENTATION NOTE: ognl 2.6.7 expression objects do not respect equality
 * properly, so the equality operations defined within this class do not
 * function properly.
 * 
 * @author Keith Donald
 */
class OgnlExpression implements PropertyExpression {

	/**
	 * The expression.
	 */
	private Object expression;

	/**
	 * Creates a new OGNL expression.
	 * @param expression the parsed expression
	 */
	public OgnlExpression(Object expression) {
		this.expression = expression;
	}

	public int hashCode() {
		return expression.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof OgnlExpression)) {
			return false;
		}
		// Ognl 2.6.7 expression objects apparently don't implement equals
		// this always returns false, which is quite nasty
		OgnlExpression other = (OgnlExpression)o;
		return expression.equals(other.expression);
	}

	public Object evaluateAgainst(Object target, Map evaluationContext) throws EvaluationException {
		try {
			Assert.notNull(target, "The target object to evaluate is required");
			if (evaluationContext == null) {
				evaluationContext = Collections.EMPTY_MAP;
			}
			return Ognl.getValue(expression, evaluationContext, target);
		}
		catch (OgnlException e) {
			throw new EvaluationException(new EvaluationAttempt(this, target, evaluationContext), e);
		}
	}

	public void setValue(Object target, Object value, Map setContext) {
		try {
			Assert.notNull(target, "The target object is required");
			if (setContext == null) {
				setContext = Collections.EMPTY_MAP;
			}
			Ognl.setValue(expression, setContext, target, value);
		}
		catch (OgnlException e) {
			throw new EvaluationException(new SetPropertyAttempt(this, target, value, setContext), e);
		}
	}

	public String toString() {
		return expression.toString();
	}
}