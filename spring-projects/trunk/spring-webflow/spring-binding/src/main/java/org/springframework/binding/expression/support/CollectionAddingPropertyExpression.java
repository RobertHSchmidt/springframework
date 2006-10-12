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

import java.util.Collection;

import org.springframework.binding.expression.EvaluationContext;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.SetValueAttempt;
import org.springframework.binding.expression.SettableExpression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A settable expression that adds non-null values to a collection. 
 * @author Keith Donald
 */
public class CollectionAddingPropertyExpression implements SettableExpression {

	/**
	 * The expression that resolves a mutable collection reference.
	 */
	private Expression collectionExpression;

	/**
	 * Creates a collection adding property expression.
	 * @param collectionExpression the collection expression
	 */
	public CollectionAddingPropertyExpression(Expression collectionExpression) {
		this.collectionExpression = collectionExpression;
	}

	public Object evaluate(Object target, EvaluationContext context) throws EvaluationException {
		return collectionExpression.evaluate(target, context);
	}

	public void evaluateToSet(Object target, Object value, EvaluationContext context) throws EvaluationException {
		Object result = evaluate(target, context);
		if (result == null) {
			throw new EvaluationException(new SetValueAttempt(this, target, value, null),
					new IllegalArgumentException("The collection expression evaluated to a [null] reference"));
		}
		Assert.isInstanceOf(Collection.class, result, "Not a collection: ");
		if (value != null) {
			// add the value to the collection
			((Collection)result).add(value);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("collectionExpression", collectionExpression).toString();
	}
}