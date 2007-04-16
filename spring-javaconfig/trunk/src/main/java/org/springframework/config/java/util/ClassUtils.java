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
package org.springframework.config.java.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.listener.ConfigurationListener;
import org.springframework.config.java.listener.ConfigurationListenerRegistry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * Convenience utility class used for translating common encountered code into
 * one-liners.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ClassUtils {

	/**
	 * Class extension.
	 */
	public static final String CLASS_EXT = ".class";

	public static final char DOT = '.';

	public static final char SLASH = '/';

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
	 * Find all bean creation methods in the given configuration class. It looks for
	 * {@link Bean} annotation on public methods.
	 * 
	 * @param configurationClass
	 * @return
	 */
	public static Collection<Method> getBeanCreationMethods(Class<?> configurationClass) {
		Assert.notNull(configurationClass);
		
		Collection<Method> beanCreationMethods = new ArrayList<Method>();
		Method[] publicMethods = configurationClass.getMethods();
		for (int i = 0; i < publicMethods.length; i++) {
			if (hasAnnotation(publicMethods[i], Bean.class)) {
				beanCreationMethods.add(publicMethods[i]);
			}
		}
		return beanCreationMethods;
	}

	/**
	 * Check if the given class is a configuration.
	 * 
	 * Additionaly, a listener registry is checked against the class.
	 * 
	 * @param candidateConfigurationClass
	 * @param registry
	 * @return
	 */
	public static boolean isConfigurationClass(Class<?> candidateConfigurationClass,
			ConfigurationListenerRegistry registry) {
		
		Assert.notNull(candidateConfigurationClass);
		if (candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !getBeanCreationMethods(candidateConfigurationClass).isEmpty()) {
			return true;
		}
		if (registry != null)
			for (ConfigurationListener cl : registry.getConfigurationListeners()) {
				if (cl.understands(candidateConfigurationClass)) {
					return true;
				}
			}
		return false;
	}

}
