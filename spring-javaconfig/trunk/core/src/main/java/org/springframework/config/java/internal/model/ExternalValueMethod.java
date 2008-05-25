package org.springframework.config.java.internal.model;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.ExternalValue;

public class ExternalValueMethod extends AbstractValidatableAnnotatedMethod<ExternalValue> {

	public ExternalValueMethod(String name, int modifiers, Annotation[] annotations) {
		super(name, modifiers, annotations);
	}

	public static boolean identifyAsExternalValueMethod(Annotation[] annotations) {
		return (findAnnotation(ExternalValue.class, annotations) != null);
	}

}
