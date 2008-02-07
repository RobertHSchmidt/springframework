package org.springframework.config.java.process;

import java.lang.reflect.Modifier;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.core.ExternalBeanMethodProcessor;
import org.springframework.config.java.core.ExternalValueMethodProcessor;
import org.springframework.config.java.core.StandardBeanMethodProcessor;
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
				&& ExternalValueMethodProcessor.findExternalValueCreationMethods(candidateConfigurationClass).isEmpty())
			return false;

		return candidateConfigurationClass.isAnnotationPresent(Configuration.class)
				|| !StandardBeanMethodProcessor.findBeanCreationMethods(candidateConfigurationClass).isEmpty();
	}

}
