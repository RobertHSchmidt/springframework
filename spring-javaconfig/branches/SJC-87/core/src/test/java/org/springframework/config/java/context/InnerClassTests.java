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
package org.springframework.config.java.context;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.InnerClassTests.Outermost.Middle.Innermost;

public class InnerClassTests {

	@Configuration
	public static class Outermost {
		@Bean
		public TestBean outermostBean() {
			return new TestBean("outer");
		}

		@Bean
		public String name() {
			return "outermost";
		}

		@Configuration
		public static class Middle {
			@Bean
			public TestBean middleBean() {
				return new TestBean("middle");
			}

			@Configuration
			public static class Innermost {
				@Bean
				public TestBean innermostBean() {
					return new TestBean("inner");
				}

				@Bean
				public String name() {
					return "innermost";
				}
			}
		}
	}

	@Configuration
	public static class Out {

		@Bean
		public TestBean outer() {
			return new TestBean("outer");
		}

		@Configuration
		public static class In {
			@Bean
			public TestBean inner() {
				return new TestBean("inner");
			}
		}

	}

	// this shouldn't throw, because even though Outermost has a declaring
	// class, the declaring class isn't a @Configuration.
	@Test
	public void testMultipleOuterConfigurationClassesThrowsExceptionOnlyIfOuterIsConfiguration() {
		new LegacyJavaConfigApplicationContext(Outermost.class, Out.In.class);
	}

	@Test(expected = RuntimeException.class)
	public void testMultipleOuterConfigurationClassesThrowsException() {
		new LegacyJavaConfigApplicationContext(Outermost.Middle.class, Out.In.class);
	}

	@Test
	public void testParentageWorks() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Out.In.class);
		assertNotNull(ctx.getParent());
		assertEquals("outer", ((TestBean) ctx.getBean("outer")).getName());
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testCannotSeeInnerConfigurationBeansFromOuterConfigurationContext() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Out.class);
		ctx.getBean("inner"); // should throw, parents can't see child beans
	}

	@Configuration
	public static class Parent {
		@Bean
		public String name() {
			return "parent";
		}
	}

	@Configuration
	public static class Child {
		@Bean
		public String name() {
			return "child";
		}
	}

	@Configuration
	public static class Child2 extends Parent {
		@Override
		@Bean
		public String name() {
			return "child";
		}
	}

	@Test
	public void foo3() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Parent.class, Child.class);
		Assert.assertEquals("child", ctx.getBean("name"));
	}

	@Test
	public void foo2() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Child2.class);
		Assert.assertEquals("child", ctx.getBean("name"));
	}

	@Test
	public void testProcessingInnerClassIncludesOuterClass() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Innermost.class);
		ctx.getBean("innermostBean");
		ctx.getBean("middleBean"); // this throws
		ctx.getBean("outermostBean"); // this throws
	}

	@Test
	public void testBeanOverride() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Innermost.class);
		assertThat(((String) ctx.getBean("name")), equalTo("innermost"));
	}

	@Configuration
	public static class One {

	}

	@Configuration
	public static class Two {

	}

	@Configuration
	public static class Three {

	}

	@Test
	public void testBeanDefs() {
		LegacyJavaConfigApplicationContext one = new LegacyJavaConfigApplicationContext(One.class);
		LegacyJavaConfigApplicationContext two = new LegacyJavaConfigApplicationContext(one);
		LegacyJavaConfigApplicationContext three = new LegacyJavaConfigApplicationContext(Three.class);

		two.setConfigClasses(Two.class);
		two.setParent(three);

		two.refresh();
	}

	@Ignore
	@Test
	public void foo() {
		LegacyJavaConfigApplicationContext ctx = new LegacyJavaConfigApplicationContext(Innermost.class);
		int actual = BeanFactoryUtils.countBeansIncludingAncestors(ctx);
		assertTrue("expected actual > 5, got " + actual, actual > 5);
	}

}
