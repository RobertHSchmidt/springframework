package org.springframework.config.java.model;

import java.lang.annotation.Annotation;

import org.springframework.util.Assert;

public class ModelMethod {

	protected final String name;
	protected final int modifiers;
	protected final Annotation[] annotations;

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


}
