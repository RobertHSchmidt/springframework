/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Chris Beams
 */
public class BeanOverridingTests {

	/**
	 * XML config works on a LIFO-based shadowing model. JavaConfig should work
	 * the same way.
	 */
	@Test
	public void demonstrateXmlShadowingIsBasedOnOrder() {
		{
			String[] configLocations = new String[] { "second.xml", "first.xml" };
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocations, getClass());
			assertEquals("first", ctx.getBean("foo"));
		}

		{
			String[] configLocations = new String[] { "first.xml", "second.xml" };
			ApplicationContext ctx = new ClassPathXmlApplicationContext(configLocations, getClass());
			assertEquals("second", ctx.getBean("foo"));
		}
	}

	@Test
	public void testShadowingIsBasedOnOrder1() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(Second.class);
		assertEquals("second", context.getBean(TestBean.class).getName());
	}

	@Test
	public void testShadowingIsBasedOnOrder2() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(Second.class, First.class);
		assertEquals("first", context.getBean(TestBean.class).getName());
	}

	@Test
	public void testShadowingIsBasedOnOrder2WorksSameWithSetter() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext();
		context.setConfigClasses(Second.class, First.class);
		context.refresh();
		assertEquals("first", context.getBean(TestBean.class).getName());
	}

	@Configuration
	public static class First {
		@Bean
		public TestBean foo() {
			return new TestBean("first");
		}
	}

	@Configuration
	public static class Second {
		@Bean
		public TestBean foo() {
			return new TestBean("second");
		}

		@Bean
		public String bar() {
			return "bar";
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testLegalShadowingViaXml() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("legalShadow.xml", getClass());
		TestBean bob = (TestBean) bf.getBean("bob");
		assertTrue(bf.containsBean("ann"));

		String msg = "Property value must have come from XML override, not @Bean method";
		assertThat(msg, "Ann", equalTo(bob.getSpouse().getName()));
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalShadowingViaXml() {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("illegalShadow.xml", getClass());
		bf.getBean("ann");
	}

	@Configuration
	static class LegalShadowConfiguration {
		@Bean
		public TestBean bob() {
			TestBean bob = new TestBean();
			bob.setSpouse(ann());
			return bob;
		}

		@Bean
		public TestBean ann() {
			return new TestBean();
		}
	}

	@Configuration
	static class IllegalShadowConfiguration {
		@Bean
		public TestBean bob() {
			TestBean bob = new TestBean();
			bob.setSpouse(ann());
			return bob;
		}

		// Does not allow overriding
		@Bean(allowOverriding = false)
		public TestBean ann() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * XML config ensures that beans defined in a child context override any
	 * beans in the parent context with the same name. JavaConfig should work
	 * the same way.
	 */
	@Test
	public void demonstrateXmlShadowingWorksProperlyWhenNestingContexts() {
		ApplicationContext parent = new ClassPathXmlApplicationContext("first.xml", getClass());
		ApplicationContext child = new ClassPathXmlApplicationContext(new String[] { "second.xml" }, getClass(), parent);
		assertEquals("first", parent.getBean("foo"));
		assertEquals("second", child.getBean("foo"));
	}

	@Test
	public void testChildContextBeanShadowsParentContextBean() {
		JavaConfigApplicationContext firstContext = new JavaConfigApplicationContext(First.class);
		JavaConfigApplicationContext secondContext = new JavaConfigApplicationContext(firstContext);
		secondContext.setConfigClasses(Second.class);
		secondContext.refresh();

		assertEquals("first", ((TestBean) firstContext.getBean("foo")).getName());
		assertEquals("second", ((TestBean) secondContext.getBean("foo")).getName());
		assertEquals("bar", secondContext.getBean("bar"));
	}

	// corners a subtle bug I found along the way...
	@Test
	public void testChildContextBeanShadowsParentContextBeanWhenUsingTypeSafeGetBeanMethod() {
		JavaConfigApplicationContext firstContext = new JavaConfigApplicationContext(First.class);
		JavaConfigApplicationContext secondContext = new JavaConfigApplicationContext(firstContext);
		secondContext.setConfigClasses(Second.class);
		secondContext.refresh();

		// assertEquals("first", firstContext.getBean(TestBean.class.getName());
		assertEquals("first", (firstContext.getBean(TestBean.class)).getName());
		assertEquals("second", (secondContext.getBean(TestBean.class)).getName());
		assertEquals("bar", secondContext.getBean(String.class));
	}

}
