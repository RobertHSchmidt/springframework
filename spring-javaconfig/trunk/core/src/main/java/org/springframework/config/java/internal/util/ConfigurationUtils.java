package org.springframework.config.java.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

@Deprecated // temporary measure
public class ConfigurationUtils {

	/**
	 * Check if the given class is a configuration.
	 *
	 * @param candidateConfigurationClass - must be non-abstract and be
	 * annotated with &#64;Configuration and/or have at least one method
	 * annotated with &#64;Bean
	 *
	 * @deprecated this method is a holdover from the ConfigurationProcessor implementation.
	 * This approach needs to know too much too early.  it would be good to get rid of it.
	 * if we can mandate that configuration classes must be annotated with
	 * {@link Configuration @Configuration}, then it gets much easier
	 */
	@Deprecated
	public static boolean isConfigurationClass(Class<?> candidateConfigurationClass) {
		Assert.notNull(candidateConfigurationClass);

		if (Modifier.isAbstract(candidateConfigurationClass.getModifiers())
				&& !hasAtLeastOneAnnotatedMethod(candidateConfigurationClass, AutoBean.class)
				&& !hasAtLeastOneAnnotatedMethod(candidateConfigurationClass, ExternalBean.class)
				&& !hasAtLeastOneAnnotatedMethod(candidateConfigurationClass, ExternalValue.class))
			return false;

		return candidateConfigurationClass.isAnnotationPresent(Configuration.class)
			|| hasAtLeastOneAnnotatedMethod(candidateConfigurationClass, Bean.class);
	}

	private static boolean hasAtLeastOneAnnotatedMethod(Class<?> configClass, final Class<? extends Annotation> targetAnnotation) {
		final ArrayList<Method> matchingMethods = new ArrayList<Method>();

		ReflectionUtils.doWithMethods(configClass, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				if(method.getAnnotation(targetAnnotation) != null)
					matchingMethods.add(method);
			}
		});

		return !matchingMethods.isEmpty();
	}

}
