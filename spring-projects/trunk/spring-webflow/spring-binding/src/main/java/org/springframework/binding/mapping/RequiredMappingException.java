package org.springframework.binding.mapping;

import org.springframework.binding.expression.Expression;

public class RequiredMappingException extends IllegalStateException {
	public RequiredMappingException(String message) {
		super(message);
	}
}
