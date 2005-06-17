package org.springframework.binding.expression;

import java.util.Map;

import org.springframework.core.ToStringCreator;

public class SetPropertyAttempt extends EvaluationAttempt {
	private Object value;

	public SetPropertyAttempt(Expression expression, Object target,
			Object value, Map context) {
		super(expression, target, context);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	protected ToStringCreator createToString(ToStringCreator creator) {
		return super.createToString(creator).append("value", value);
	}
}
