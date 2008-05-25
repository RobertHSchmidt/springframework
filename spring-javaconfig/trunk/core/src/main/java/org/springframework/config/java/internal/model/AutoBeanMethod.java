package org.springframework.config.java.internal.model;

import static java.lang.String.format;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.model.ModelClass;
import org.springframework.config.java.model.ModelMethod;
import org.springframework.util.Assert;

public class AutoBeanMethod extends ModelMethod {

	private final AutoBean metadata;
	private final ModelClass returnType;

	public AutoBeanMethod(String name, ModelClass returnType, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);
		this.metadata = findAnnotation(AutoBean.class, annotations);
		Assert.notNull(metadata);
		this.returnType = returnType;
	}

	public static boolean identifyAsExternalBeanMethod(Annotation[] annotations) {
		return (findAnnotation(AutoBean.class, annotations) != null);
	}

	public AutoBean getMetadata() {
		return metadata;
	}

	public ModelClass getReturnType() {
		return returnType;
	}

	public void validate(ValidationErrors errors) {
		if(returnType.isInterface())
			errors.add(ValidationError.AUTOBEAN_MUST_BE_CONCRETE_TYPE.toString());
	}

	@Override
	public String toString() {
		return format("%s; returnType=%s", super.toString(), returnType.getSimpleName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
		AutoBeanMethod other = (AutoBeanMethod) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
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
