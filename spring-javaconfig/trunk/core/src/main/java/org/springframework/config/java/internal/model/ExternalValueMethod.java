package org.springframework.config.java.internal.model;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.model.ModelMethod;

public class ExternalValueMethod extends ModelMethod {

	public ExternalValueMethod(String name, int modifiers, Annotation[] annotations) {
		super(name, modifiers, annotations);
	}

	public void validate(ValidationErrors errors) {
		// TODO Auto-generated method stub
	}

	public static boolean identifyAsExternalValueMethod(Annotation[] annotations) {
		return (findAnnotation(ExternalValue.class, annotations) != null);
	}

}
