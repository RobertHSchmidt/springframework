/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java.model;

import static java.lang.String.format;
import static org.springframework.config.java.model.AnnotationExtractionUtils.extractMethodAnnotation;

import java.lang.reflect.Modifier;

import org.springframework.config.java.annotation.Bean;
import org.springframework.util.Assert;

public class BeanMethod {
	private static final Bean DEFAULT_BEAN_ANNOTATION =
		extractMethodAnnotation(Bean.class, new MethodAnnotationPrototype() { public @Bean void targetMethod() {} }.getClass());
	private final String name;
	private final Bean beanAnnotation;
	private final int modifiers;

	/** for testing convenience */
	BeanMethod(String name) { this(name, DEFAULT_BEAN_ANNOTATION); }

	/** for testing convenience */
	BeanMethod(String name, int modifiers) { this(name, DEFAULT_BEAN_ANNOTATION, modifiers); }

	public BeanMethod(String name, Bean beanAnno) { this(name, beanAnno, 0); }

	public BeanMethod(String name, Bean beanAnno, int modifiers) {
		Assert.hasText(name);
		this.name = name;

		Assert.notNull(beanAnno);
		this.beanAnnotation = beanAnno;

		Assert.isTrue(modifiers >= 0, "modifiers must be non-negative: " + modifiers);
		this.modifiers = modifiers;
	}

	public String getName() {
		return name;
	}

	public Bean getMetadata() {
		return beanAnnotation;
	}

	/** @see java.lang.reflect.Modifier */
	public int getModifiers() {
		return modifiers;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(Modifier.isPrivate(modifiers))
			// TODO: needs to have reference to parent class for better diagnostics
			errors.add(ValidationError.METHOD_MAY_NOT_BE_PRIVATE + ": " + this.getName());
		return errors;
	}

	@Override
	public String toString() {
		return format("%s: name=%s; modifiers=%d",
				       getClass().getSimpleName(), name, modifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanAnnotation == null) ? 0 : beanAnnotation.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + modifiers;
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
		BeanMethod other = (BeanMethod) obj;
		if (beanAnnotation == null) {
			if (other.beanAnnotation != null)
				return false;
		}
		else if (!beanAnnotation.equals(other.beanAnnotation))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (modifiers != other.modifiers)
			return false;
		return true;
	}

}