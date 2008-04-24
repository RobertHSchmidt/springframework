package org.springframework.config.java.model;

// TODO: these errors are awkward (see their usage). devise a better scheme.
public enum ValidationError {
	MODEL_IS_EMPTY,
	CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_BEAN,
	METHOD_MAY_NOT_BE_PRIVATE,
	ILLEGAL_BEAN_OVERRIDE,
	ABSTRACT_CONFIGURATION_MUST_DECLARE_AT_LEAST_ONE_EXTERNALBEAN,
	CONFIGURATION_MUST_BE_NON_FINAL
}
