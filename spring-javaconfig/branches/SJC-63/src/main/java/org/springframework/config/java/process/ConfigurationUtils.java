package org.springframework.config.java.process;

import java.lang.reflect.Modifier;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

public class ConfigurationUtils {

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
				&& ExternalBeanMethodProcessor.findExternalBeanCreationMethods(candidateConfigurationClass).isEmpty()
				&& ExternalValueMethodProcessor.findExternalValueCreationMethods(candidateConfigurationClass).isEmpty()
				&& AutoBeanMethodProcessor.findAutoBeanCreationMethods(candidateConfigurationClass).isEmpty()
				&& AnnotationUtils.findAnnotation(candidateConfigurationClass, Aspect.class) == null)
			return false;

		return candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !StandardBeanMethodProcessor.findBeanCreationMethods(candidateConfigurationClass).isEmpty();
	}

}
