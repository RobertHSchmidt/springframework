package org.springframework.config.java.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class PropagateBeanNamingTests extends AbstractDependencyInjectionSpringContextTests {
	/**
	 * We are autowired by name and the naming strategy will name our bean
	 * "test" + [method name]. So we expect this property to be set with
	 * TestConfiguration.bean().
	 */
	private Object testbean;

	@Configuration
	public static class TestConfiguration {
		@Bean
		public Object bean() {
			return "Test";
		}
	}

	public static class TestNamingStrategy implements BeanNamingStrategy {
		public String getBeanName(Method pBeanCreationMethod) {
			return "test" + pBeanCreationMethod.getName();
		}
	}

	public void testNamingStrategy() {
		assertEquals("Test", testbean);
	}

	public PropagateBeanNamingTests() {
		setAutowireMode(AUTOWIRE_BY_NAME);
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/config/java/naming/PropagateBeanNamingTest.xml", };
	}

	public Object getTestbean() {
		return testbean;
	}

	public void setTestbean(Object pTestbean) {
		testbean = pTestbean;
	}
}
