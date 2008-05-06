package org.springframework.config.java.model;

import static org.springframework.config.java.model.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.ExternalValue;

public class ExternalValueMethod {

	public ExternalValueMethod(String name, int modifiers, Annotation[] annotations) {
		// TODO Auto-generated constructor stub
	}

	public void validate(ValidationErrors errors) {
		// TODO Auto-generated method stub
	}

	public static boolean identifyAsExternalValueMethod(Annotation[] annotations) {
		return (findAnnotation(ExternalValue.class, annotations) != null);
	}

}
