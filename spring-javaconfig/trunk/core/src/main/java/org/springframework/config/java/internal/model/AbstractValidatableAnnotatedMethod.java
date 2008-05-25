package org.springframework.config.java.internal.model;

import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.config.java.model.ModelMethod;
import org.springframework.util.Assert;


abstract class AbstractValidatableAnnotatedMethod<A extends Annotation>
                                                 extends ModelMethod
                                                 implements ValidatableMethod, AnnotatedMethod<A> {

	private final A metadata;

	public AbstractValidatableAnnotatedMethod(String name, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);
		Type superclass = getClass().getGenericSuperclass();
		if(!(superclass instanceof ParameterizedType))
			throw new IllegalStateException("must subclass parameterized type");

		ParameterizedType pSuper = (ParameterizedType) superclass;
		Type[] typeArgs = pSuper.getActualTypeArguments();

		Type arg = typeArgs[0];

		Assert.isInstanceOf(Class.class, arg);

		@SuppressWarnings("unchecked")
		Class<? extends Annotation> annoType = (Class<? extends Annotation>) arg;

		this.metadata = (A) findAnnotation(annoType, annotations);
		Assert.notNull(metadata, "could not find target annotation @" + annoType.getName());
	}

	public A getMetadata() {
		return metadata;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(Modifier.isPrivate(getModifiers()))
			// TODO: needs to have reference to parent class for better diagnostics
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
		AbstractValidatableAnnotatedMethod<?> other = (AbstractValidatableAnnotatedMethod<?>) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		return true;
	}

}
