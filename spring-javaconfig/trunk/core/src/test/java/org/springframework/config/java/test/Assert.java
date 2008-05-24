package org.springframework.config.java.test;

import static org.junit.Assert.assertEquals;

import org.springframework.config.java.util.Constants;
import org.springframework.context.ConfigurableApplicationContext;

public class Assert {
	/**
	 * Validates the number of beans registered with <var>ctx</var> matches
	 * <var>expected</var>. Makes a special exception, however for any bean
	 * definitions that have the {@link Constants#JAVA_CONFIG_IGNORE}.  These
	 * definitions are considered 'internal' and not to be counted.
	 *
	 * @param ctx context to interrogate
	 * @param expected expected number of beans
	 */
	public static void assertBeanDefinitionCount(ConfigurableApplicationContext ctx, int expected) {
		int actual = getNonInternalBeanDefinitionCount(ctx);
		assertEquals(expected, actual);
	}

	/**
	 * Returns the number of beans in this <var>ctx</var> that are not JavaConfig-internal bean definitions.
	 * Said another way, returns the number of beans that the end user has registered.
	 */
	public static int getNonInternalBeanDefinitionCount(ConfigurableApplicationContext ctx) {
		int cx = 0;
		for (String name : ctx.getBeanDefinitionNames())
			if(ctx.getBeanFactory().getBeanDefinition(name).getAttribute(Constants.JAVA_CONFIG_IGNORE) == null)
				cx++;
		return cx;
	}
}
