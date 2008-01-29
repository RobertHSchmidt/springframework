/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * Convenience utility class used for translating common encountered code into
 * one-liners.
 * 
 * @author Costin Leau
 * 
 */
public final class ClassUtils {

	/** enforce un-instantiability */
	private ClassUtils() {
	}

	/**
	 * Class extension.
	 */
	public static final String CLASS_EXT = ".class";

	public static final char DOT = '.';

	public static final char SLASH = '/';

	public static final String JAVA_CONFIG_PKG = "org.springframework.config.java";

	/**
	 * Convert the / form to the . form
	 * @param className
	 * @return
	 */
	public static String classNameInternalToLoadable(String className) {
		return className.replace(SLASH, DOT);
	}

	/**
	 * Converts . form to / form.
	 * @param className
	 * @return
	 */
	public static String classNameLoadableToInternal(String className) {
		return className.replace(DOT, SLASH);
	}

	/**
	 * Check if the given method has the given annotation.
	 * 
	 * @param method
	 * @param a
	 * @return
	 */
	public static boolean hasAnnotation(Method method, Class<? extends Annotation> a) {
		Assert.notNull(method, "method is required");
		Assert.notNull(a, "annotation is required");

		return (AnnotationUtils.findAnnotation(method, a) != null);
	}

	/**
	 * Check if the given class is a configuration.
	 * 
	 * @param candidateConfigurationClass - must be non-abstract and be
	 * annotated with &#64;Configuration and/or have at least one method
	 * annotated with &#64;Bean
	 */
	public static boolean isConfigurationClass(Class<?> candidateConfigurationClass) {
		Assert.notNull(candidateConfigurationClass);

		if (Modifier.isAbstract(candidateConfigurationClass.getModifiers())
				&& getExternalBeanCreationMethods(candidateConfigurationClass).isEmpty()
				&& getExternalValueCreationMethods(candidateConfigurationClass).isEmpty())
			return false;

		return candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !getBeanCreationMethods(candidateConfigurationClass).isEmpty();
	}

	/**
	 * Find all bean creation methods in the given configuration class. It looks
	 * for {@link Bean} annotation on public methods.
	 * 
	 * @param configurationClass
	 * @return
	 */
	public static Collection<Method> getBeanCreationMethods(Class<?> configurationClass) {
		return getAnnotatedMethods(configurationClass, Bean.class);
	}

	/**
	 * Find all methods that are annotated with &#64;ExternalBean.
	 * 
	 * @param configurationClass
	 * @return collection of all methods annotated with {@link ExternalBean}
	 */
	public static Collection<Method> getExternalBeanCreationMethods(Class<?> configurationClass) {
		return getAnnotatedMethods(configurationClass, ExternalBean.class);
	}

	/**
	 * Find all methods that are annotated with &#64;ExternalValue.
	 * 
	 * @param configurationClass
	 * @return collection of all methods annotated with {@link ExternalValue}
	 */
	public static Collection<Method> getExternalValueCreationMethods(Class<?> configurationClass) {
		return getAnnotatedMethods(configurationClass, ExternalValue.class);
	}

	public static Collection<Method> getAnnotatedMethods(Class<?> targetClass,
			Class<? extends Annotation> targetAnnotation) {
		Assert.notNull(targetClass);
		Assert.notNull(targetAnnotation);

		Collection<Method> matchingMethods = new ArrayList<Method>();
		Method[] publicMethods = targetClass.getMethods();
		for (int i = 0; i < publicMethods.length; i++) {
			if (ClassUtils.hasAnnotation(publicMethods[i], targetAnnotation)) {
				matchingMethods.add(publicMethods[i]);
			}
		}
		return matchingMethods;
	}

}
