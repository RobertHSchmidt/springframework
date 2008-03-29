package org.springframework.config.java.test;

import static org.junit.Assert.assertEquals;

import org.springframework.config.java.aspects.RequiredAnnotationMethodInvocationMonitor;
import org.springframework.context.ConfigurableApplicationContext;

public class Assert {
	/**
	 * Validates the number of beans registered with <var>ctx</var> matches
	 * <var>expected</var>. Makes a special exception, however for the special
	 * BeanPostProcessor registered for clearing out the
	 * RequiredAnnotationMethodInvocationMonitor aspect
	 * 
	 * @param ctx context to interrogate
	 * @param expected expected number of beans
	 */
	public static void assertBeanDefinitionCount(ConfigurableApplicationContext ctx, int expected) {
		int actual = 0;
		for (String name : ctx.getBeanDefinitionNames()) {
			String beanClassName = ctx.getBeanFactory().getBeanDefinition(name).getBeanClassName();
			String targetClassName = RequiredAnnotationMethodInvocationMonitor.PostProcessor.class.getName();
			if (!targetClassName.equals(beanClassName))
				actual++;
		}
		assertEquals(expected, actual);
	}
}
