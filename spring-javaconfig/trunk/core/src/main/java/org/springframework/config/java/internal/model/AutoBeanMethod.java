package org.springframework.config.java.internal.model;

import static java.lang.String.format;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.type.Type;
import org.springframework.util.Assert;

public class AutoBeanMethod {

	private final String name;
	private final AutoBean metadata;
	private final Type returnType;
	private final int modifiers;

	public AutoBeanMethod(String name, Type returnType, int modifiers, Annotation... annotations) {
		this.name = name;
		Assert.notNull(annotations);
		this.metadata = findAnnotation(AutoBean.class, annotations);
		Assert.notNull(metadata);
		this.returnType = returnType;
		this.modifiers = modifiers;
	}

	public static boolean identifyAsExternalBeanMethod(Annotation[] annotations) {
		return (findAnnotation(AutoBean.class, annotations) != null);
	}

	public String getName() {
		return name;
	}

	public AutoBean getMetadata() {
		return metadata;
	}

	public Type getReturnType() {
		return returnType;
	}

	public void validate(ValidationErrors errors) {
		if(returnType.isInterface())
			errors.add(ValidationError.AUTOBEAN_MUST_BE_CONCRETE_TYPE.toString());
	}

	@Override
	public String toString() {
		return format("%s: name=%s; returnType=%s; modifiers=%d",
				       getClass().getSimpleName(), name, returnType.getSimpleName(), modifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutoBeanMethod other = (AutoBeanMethod) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		}
		else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

}
