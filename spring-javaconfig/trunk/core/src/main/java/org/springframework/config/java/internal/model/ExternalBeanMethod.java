package org.springframework.config.java.internal.model;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.extractMethodAnnotation;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.internal.util.MethodAnnotationPrototype;
import org.springframework.config.java.model.ModelMethod;
import org.springframework.util.Assert;

public class ExternalBeanMethod extends ModelMethod {

	private static final ExternalBean defaultAnno = extractMethodAnnotation(ExternalBean.class, new MethodAnnotationPrototype() {
		public @ExternalBean void targetMethod() { }
	}.getClass());

	private final ExternalBean metadata;

	/** for testing convenience */
	ExternalBeanMethod(String name) { this(name, defaultAnno); }

	/** for testing convenience */
	ExternalBeanMethod(String name, int modifiers) { this(name, modifiers, defaultAnno); }

	public ExternalBeanMethod(String name, Annotation... annotations) { this(name, 0, annotations); }

	public ExternalBeanMethod(String name, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);

		this.metadata = findAnnotation(ExternalBean.class, annotations);
		Assert.notNull(metadata);
	}

	public static boolean identifyAsExternalBeanMethod(Annotation[] annotations) {
		return (findAnnotation(ExternalBean.class, annotations) != null);
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(Modifier.isPrivate(getModifiers()))
			// TODO: needs to have reference to parent class for better diagnostics
			// TODO: needs to distinguish that this is an @ExternalBean vs @Bean
			errors.add(ValidationError.METHOD_MAY_NOT_BE_PRIVATE + ": " + getName());
		return errors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalBeanMethod other = (ExternalBeanMethod) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		return true;
	}

}