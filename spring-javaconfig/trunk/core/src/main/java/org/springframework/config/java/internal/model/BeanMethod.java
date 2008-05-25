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
package org.springframework.config.java.internal.model;

import static java.lang.String.format;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.extractMethodAnnotation;
import static org.springframework.config.java.internal.util.AnnotationExtractionUtils.findAnnotation;
import static org.springframework.config.java.util.DefaultScopes.PROTOTYPE;
import static org.springframework.config.java.util.DefaultScopes.SINGLETON;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.HotSwappable;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.internal.util.AnnotationExtractionUtils;
import org.springframework.config.java.internal.util.MethodAnnotationPrototype;
import org.springframework.config.java.type.ModelMethod;
import org.springframework.util.Assert;

public class BeanMethod extends ModelMethod {
	private static final Bean DEFAULT_BEAN_ANNOTATION =
		extractMethodAnnotation(Bean.class, new MethodAnnotationPrototype() { public @Bean void targetMethod() {} }.getClass());
	private final Bean metadata;
	private final ScopedProxy scopedProxyMetadata;

	/** for testing convenience */
	BeanMethod(String name) { this(name, DEFAULT_BEAN_ANNOTATION); }

	/** for testing convenience */
	BeanMethod(String name, int modifiers) { this(name, modifiers, DEFAULT_BEAN_ANNOTATION); }

	public BeanMethod(String name, Annotation... annotations) { this(name, 0, annotations); }

	/**
	 *
	 * @param name
	 * @param modifiers
	 * @param annotations list of any JavaConfig-specific annotations
	 * (must be within org.springframework.config.java)
	 */
	public BeanMethod(String name, int modifiers, Annotation... annotations) {
		super(name, modifiers, annotations);

		this.metadata = findAnnotation(Bean.class, annotations);
		Assert.notNull(metadata, "could not find target annotation @" + Bean.class.getName());

		// may be null, it's ok
		this.scopedProxyMetadata = findAnnotation(ScopedProxy.class, annotations);
	}

	/**
	 * Determines whether this array of method <var>annotations</var> represents a Bean method
	 */
	public static boolean identifyAsBeanMethod(Annotation[] annotations) {
		return (AnnotationExtractionUtils.findAnnotation(Bean.class, annotations) != null);
	}

	public Bean getMetadata() {
		return metadata;
	}


	public boolean isScopedProxy() {
		return scopedProxyMetadata != null;
	}

	public ScopedProxy getScopedProxyMetadata() {
		return scopedProxyMetadata;
	}

	public ValidationErrors validate(ValidationErrors errors) {
		if(Modifier.isPrivate(modifiers))
			// TODO: needs to have reference to parent class for better diagnostics
			errors.add(ValidationError.METHOD_MAY_NOT_BE_PRIVATE + ": " + name);

		Object[] compatibleAnnotationTypes = new Object[] { Bean.class, ScopedProxy.class, HotSwappable.class };

		for(Annotation anno : annotations)
			if(!arrayContains(compatibleAnnotationTypes, anno.annotationType()))
				errors.add(String.format("%s: @%s method is not compatible with @%s",
						ValidationError.INCOMPATIBLE_ANNOTATION,
						metadata.annotationType().getSimpleName(),
						anno.annotationType().getSimpleName()));

		if(this.isScopedProxy())
			if(metadata.scope().equals(SINGLETON) || metadata.scope().equals(PROTOTYPE))
				errors.add(format("%s: method %s contains an invalid annotation declaration: @ScopedProxy "
								+ "cannot be used on a singleton/prototype bean",
								ValidationError.INVALID_ANNOTATION_DECLARATION, name));

		return errors;
	}

	private boolean arrayContains(Object[] array, Object item) {
		for(Object candidate : array)
			if(candidate.equals(item))
				return true;

		return false;
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
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
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
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
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