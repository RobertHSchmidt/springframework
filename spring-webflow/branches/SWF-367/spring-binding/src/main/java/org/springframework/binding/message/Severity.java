package org.springframework.binding.message;

import org.springframework.core.enums.StaticLabeledEnum;

public class Severity extends StaticLabeledEnum {

	public static final Severity INFO = new Severity(0, "Info");

	public static final Severity WARNING = new Severity(1, "Warning");

	public static final Severity ERROR = new Severity(2, "Error");

	private Severity(int code, String label) {
		super(code, label);
	}
}
