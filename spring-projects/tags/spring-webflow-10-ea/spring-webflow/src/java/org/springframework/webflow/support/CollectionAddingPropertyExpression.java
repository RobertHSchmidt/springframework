package org.springframework.webflow.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.expression.SetPropertyAttempt;
import org.springframework.core.style.ToStringCreator;

/**
 * A property expression that can add an element to a collection when asked to
 * set its value.
 * @author Keith Donald
 */
public class CollectionAddingPropertyExpression implements PropertyExpression {

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

	public void setValue(Object target, Object value, Map context) throws EvaluationException {
		Collection collection = (Collection)evaluateAgainst(target, context);
		if (collection == null) {
			throw new EvaluationException(new SetPropertyAttempt(collectionExpression, target, value, context),
					new IllegalArgumentException("The collection expression evaluated to a [null] reference"));
		}
		if (value != null) {
			collection.add(value);
		}
	}

	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		return collectionExpression.evaluateAgainst(target, context);
	}

	public String toString() {
		return new ToStringCreator(this).append("collectionExpression", collectionExpression).toString();
	}
}