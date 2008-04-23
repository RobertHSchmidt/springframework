package org.springframework.config.java.test;

import static org.junit.Assert.assertEquals;

import org.springframework.config.java.core.Constants;
import org.springframework.context.ConfigurableApplicationContext;

public class Assert {
	/**
	 * Validates the number of beans registered with <var>ctx</var> matches
	 * <var>expected</var>. Makes a special exception, however for the special
	 * BeanPostProcessor registered for clearing out the
	 * {@link org.springframework.config.java.aspects.RequiredMethodInvocationTracker.RequiredAnnotationMethodInvocationMonitor}
	 * aspect
	 *
	 * @param ctx context to interrogate
	 * @param expected expected number of beans
	 */
	public static void assertBeanDefinitionCount(ConfigurableApplicationContext ctx, int expected) {
		int actual = 0;
		for (String name : ctx.getBeanDefinitionNames())
			if(ctx.getBeanFactory().getBeanDefinition(name).getAttribute(Constants.JAVA_CONFIG_IGNORE) == null)
				actual++;
		assertEquals(expected, actual);
	}
}
