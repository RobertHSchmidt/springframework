package org.springframework.binding.expression;

public class ExpressionVariable {
	private String name;
	private String value;

	public ExpressionVariable(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
