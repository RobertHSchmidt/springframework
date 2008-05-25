package org.springframework.config.java.internal.model;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.extractMethodAnnotation;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.internal.util.MethodAnnotationPrototype;

public class ExternalBeanMethod extends AbstractValidatableAnnotatedMethod<ExternalBean> {

	private static final ExternalBean defaultAnno = extractMethodAnnotation(ExternalBean.class, new MethodAnnotationPrototype() {
		public @ExternalBean void targetMethod() { }
	}.getClass());

	/** for testing convenience */
	ExternalBeanMethod(String name) { this(name, defaultAnno); }

	/** for testing convenience */
	ExternalBeanMethod(String name, int modifiers) { this(name, modifiers, defaultAnno); }

	public ExternalBeanMethod(String name, Annotation... annotations) { this(name, 0, annotations); }

	public ExternalBeanMethod(String name, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);
	}

	public static boolean identifyAsExternalBeanMethod(Annotation[] annotations) {
		return (findAnnotation(ExternalBean.class, annotations) != null);
	}
}