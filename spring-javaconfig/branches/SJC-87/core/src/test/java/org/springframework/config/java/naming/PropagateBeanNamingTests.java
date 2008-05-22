/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.naming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.model.ModelMethod;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PropagateBeanNamingTests {

	@Configuration
	public static class TestConfiguration {
		public @Bean Object bean() {
			return "Test";
		}
	}

	public static class TestNamingStrategy implements BeanNamingStrategy {
		public String getBeanName(Method pBeanCreationMethod) {
			return "test" + pBeanCreationMethod.getName();
		}

    	public String getBeanName(ModelMethod modelMethod) {
			return "test" + modelMethod.getName();
    	}
	}

	public @Test void testNamingStrategyViaXml() {
		ClassPathXmlApplicationContext ctx =
			new ClassPathXmlApplicationContext("PropagateBeanNamingTests.xml", PropagateBeanNamingTests.class);

		assertContextContainsProperlyNamedBean(ctx);
	}

	public @Test void testNamingStrategyViaJcac() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.setBeanNamingStrategy(new TestNamingStrategy());
		ctx.addConfigClasses(TestConfiguration.class);
		ctx.refresh();

		assertContextContainsProperlyNamedBean(ctx);
	}

	public @Test void testNamingStrategyViaJcacSubclass() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(TestConfiguration.class) {
			@Override
			public BeanNamingStrategy getBeanNamingStrategy() {
				return new TestNamingStrategy();
			}
		};

		assertContextContainsProperlyNamedBean(ctx);
	}

	private void assertContextContainsProperlyNamedBean(AbstractApplicationContext ctx) {
		assertTrue("context should have contained bean named 'testbean'." +
				"actual contents of bean factory were: " + ctx.getBeanFactory(),
				ctx.containsBean("testbean"));

		assertEquals("Test", ctx.getBean("testbean"));
	}


}
