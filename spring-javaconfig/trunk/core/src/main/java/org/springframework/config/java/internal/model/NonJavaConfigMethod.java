package org.springframework.config.java.internal.model;

import java.lang.annotation.Annotation;

import org.springframework.config.java.model.ModelMethod;

public class NonJavaConfigMethod extends ModelMethod implements ValidatableMethod {

	NonJavaConfigMethod(String name) {
		this(name, 0);
	}

	public NonJavaConfigMethod(String name, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);
	}

	public ValidationErrors validate(ValidationErrors errors) {
		for(Annotation anno : getAnnotations())
			errors.add(ValidationError.INVALID_ANNOTATION_DECLARATION + ": " + getName() + " " + anno.annotationType().getSimpleName());
		return errors;
	}

}