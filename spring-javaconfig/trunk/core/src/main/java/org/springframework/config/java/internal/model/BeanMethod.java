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

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.HotSwappable;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.internal.util.AnnotationExtractionUtils;
import org.springframework.config.java.internal.util.MethodAnnotationPrototype;

public class BeanMethod extends AbstractValidatableAnnotatedMethod<Bean> {

	private static final Bean DEFAULT_BEAN_ANNOTATION =
		extractMethodAnnotation(Bean.class, new MethodAnnotationPrototype() { public @Bean void targetMethod() {} }.getClass());

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

		// may be null, it's ok
		this.scopedProxyMetadata = findAnnotation(ScopedProxy.class, annotations);
	}

	/**
	 * Determines whether this array of method <var>annotations</var> represents a Bean method
	 */
	public static boolean identifyAsBeanMethod(Annotation[] annotations) {
		return (AnnotationExtractionUtils.findAnnotation(Bean.class, annotations) != null);
	}

	public boolean isScopedProxy() {
		return scopedProxyMetadata != null;
	}

	public ScopedProxy getScopedProxyMetadata() {
		return scopedProxyMetadata;
	}

	@Override
	public ValidationErrors validate(ValidationErrors errors) {
		super.validate(errors);

		Object[] compatibleAnnotationTypes = new Object[] { Bean.class, ScopedProxy.class, HotSwappable.class };

		for(Annotation anno : getAnnotations())
			if(!arrayContains(compatibleAnnotationTypes, anno.annotationType()))
				errors.add(String.format("%s: @%s method is not compatible with @%s",
						ValidationError.INCOMPATIBLE_ANNOTATION,
						getMetadata().annotationType().getSimpleName(),
						anno.annotationType().getSimpleName()));

		if(this.isScopedProxy())
			if(getMetadata().scope().equals(SINGLETON) || getMetadata().scope().equals(PROTOTYPE))
				errors.add(format("%s: method %s contains an invalid annotation declaration: @ScopedProxy "
								+ "cannot be used on a singleton/prototype bean",
								ValidationError.INVALID_ANNOTATION_DECLARATION, getName()));

		return errors;
	}

	private boolean arrayContains(Object[] array, Object item) {
		for(Object candidate : array)
			if(candidate.equals(item))
				return true;

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((scopedProxyMetadata == null) ? 0 : scopedProxyMetadata.hashCode());
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
		BeanMethod other = (BeanMethod) obj;
		if (scopedProxyMetadata == null) {
			if (other.scopedProxyMetadata != null)
				return false;
		}
		else if (!scopedProxyMetadata.equals(other.scopedProxyMetadata))
			return false;
		return true;
	}

}