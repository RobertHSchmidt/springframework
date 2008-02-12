package org.springframework.config.java.process;


import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.process.StandardBeanMethodProcessor;

public class StandardBeanMethodProcessorTests {

	@Test
	public void privateBeanMethodsNotSupported() throws SecurityException, NoSuchMethodException {
		Method privateBeanMethod = MyConfig.class.getDeclaredMethod("invalid");
		Assert.assertFalse(StandardBeanMethodProcessor.isBeanCreationMethod(privateBeanMethod));
	}

	static class MyConfig {
		@SuppressWarnings("unused")
		@Bean
		private TestBean invalid() {
			return new TestBean("invalid");
		}
	}
}
