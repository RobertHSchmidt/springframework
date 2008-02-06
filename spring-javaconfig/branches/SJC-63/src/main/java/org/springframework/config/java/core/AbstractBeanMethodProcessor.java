package org.springframework.config.java.core;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

abstract class AbstractBeanMethodProcessor implements BeanMethodProcessor {
	protected final Logger log = Logger.getLogger(this.getClass());

	private final Class<? extends Annotation> annotation;

	protected AbstractBeanMethodProcessor(Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}

	protected static Set<Method> findAllMethods(Class<?> configurationClass) {
		HashSet<Method> allMethods = new HashSet<Method>();

		for (Method method : configurationClass.getDeclaredMethods())
			allMethods.add(method);

		for (Method method : configurationClass.getMethods())
			allMethods.add(method);

		return allMethods;
	}

	protected boolean isAnnotatedAndNonPrivate(Method candidateMethod) {
		if (AnnotationUtils.findAnnotation(candidateMethod, annotation) != null)
			if (Modifier.isPrivate(candidateMethod.getModifiers()))
				log.warn(format("ignoring @%s method %s.%s(): private visibility is not supported", //
						annotation.getSimpleName(), //
						candidateMethod.getDeclaringClass().getSimpleName(), //
						candidateMethod.getName()));
			else
				return true;

		return false;
	}

	protected boolean understands(Method candidateMethod) {
		return isAnnotatedAndNonPrivate(candidateMethod);
	}

	protected Collection<Method> findMatchingMethods(Class<?> configurationClass) {
		HashSet<Method> matchingMethods = new HashSet<Method>();

		for (Method method : findAllMethods(configurationClass))
			if (understands(method))
				matchingMethods.add(method);

		return matchingMethods;
	}

}
