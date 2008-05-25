package org.springframework.config.java.model;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.springframework.util.Assert;

public class ModelMethod {

	private final String name;
	private final int modifiers;
	private final Annotation[] annotations;

	/** optionally set */
	private ModelClass declaringClass;

	public ModelMethod(String name, int modifiers, Annotation[] annotations) {
		Assert.hasText(name);
		this.name = name;

		Assert.notNull(annotations);
		this.annotations = annotations;

		Assert.isTrue(modifiers >= 0, "modifiers must be non-negative: " + modifiers);
		this.modifiers = modifiers;
	}

	public String getName() {
		return name;
	}

	/** @see java.lang.reflect.Modifier */
	public int getModifiers() {
		return modifiers;
	}

	protected Annotation[] getAnnotations() {
		return annotations;
	}

	public void setDeclaringClass(ModelClass declaringClass) {
		this.declaringClass = declaringClass;
	}

	public ModelClass getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Create a ModelMethod representation of a {@link java.lang.reflect.Method}
	 */
	public static ModelMethod forMethod(java.lang.reflect.Method method) {
		ModelMethod modelMethod = new ModelMethod(method.getName(), method.getModifiers(), method.getAnnotations());
		modelMethod.setDeclaringClass(ModelClass.forClass(method.getDeclaringClass()));
		return modelMethod;
	}

	/* NOTE: bi-directional relationship between ModelMethod and ModelClass means that
	 * hashCode() should not evaluate declaringClass.  It will cause a stack overflow */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(annotations);
		result = prime * result + modifiers;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* NOTE: bi-directional relationship between ModelMethod and ModelClass means that
	 * equals() should not evaluate declaringClass.  It will cause a stack overflow */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelMethod other = (ModelMethod) obj;
		if (!Arrays.equals(annotations, other.annotations))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}


}
