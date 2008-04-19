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

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Primary;

public class JavaConfigApplicationContextTypeSafeGetBeanMethodTests {

	private ConfigurableJavaConfigApplicationContext ctx;

	/** happy path */
	@Test
	public void testGetBeanOfTypeT() {
		ctx = new JavaConfigApplicationContext(SingleBeanConfig.class);

		TestBean testBean = ctx.getBean(TestBean.class);

		assertNotNull("return value should never be null", testBean);
		assertThat(testBean.getName(), equalTo("service"));
	}

	@Test(expected = AmbiguousBeanLookupException.class)
	public void testGetBeanByTypeWithMultipleCanditates() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(TestBean.class); // will throw
	}

	/**
	 * Tests that given two beans having the same supertype but one having a
	 * more specific concrete types than the other, looking up the more specific
	 * bean by it's concrete type succeeds
	 */
	@Test
	public void testDisambiguationBySubclass() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(MyTestBean.class);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testNoSuchBeanDefinitionException() {
		ctx = new JavaConfigApplicationContext(SingleBeanConfig.class);
		ctx.getBean(String.class);
	}

	@Test
	public void testDisambiguationByPrimaryDesignation() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfigWithPrimary.class);
		ctx.getBean(TestBean.class);
	}

	@Test(expected = MultiplePrimaryBeanDefinitionException.class)
	public void testCannotDisambiguateWithMultiplePrimaryDesignations() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfigWithMultiplePrimaries.class);
		ctx.getBean(TestBean.class);
	}

	@Test
	public void testDisambiguateByProvidingQualifyingBeanName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		TestBean serviceA = ctx.getBean(TestBean.class, "serviceA");
		assertNotNull(serviceA);
	}

	@Test(expected = BeanNotOfRequiredTypeException.class)
	public void testDisambiguateByProvidingQualifyingBeanNameWithWrongClassName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(String.class, "serviceA"); // throws
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testDisambiguateByProvidingQualifyingBeanNameWithWrongBeanName() {
		ctx = new JavaConfigApplicationContext(MultiBeanConfig.class);
		ctx.getBean(TestBean.class, "serviceX"); // throws
	}

	@Test
	public void testInnerConfigurationContextHierarchyWorksWhenDoingStringBasedLookup() {
		ctx = new LegacyJavaConfigApplicationContext(OuterConfig.InnerConfig.class);
		TestBean testBean = (TestBean) ctx.getBean("testBean");
		assertEquals("outer", testBean.getName());
	}

	@Test
	public void testInnerConfigurationContextHierarchyWorksWhenDoingTypeSafeLookup() {
		ctx = new LegacyJavaConfigApplicationContext(OuterConfig.InnerConfig.class);
		TestBean testBean = ctx.getBean(TestBean.class);
		assertEquals("outer", testBean.getName());
	}

	@Configuration
	static class SingleBeanConfig {
		@Bean
		public TestBean service() {
			return new TestBean("service");
		}
	}

	@Configuration
	static class MultiBeanConfig {
		@Bean
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean
		public MyTestBean serviceB() {
			return new MyTestBean("serviceB");
		}
	}

	static class MyTestBean extends TestBean {
		public MyTestBean(String name) {
			super(name);
		}
	}

	@Configuration
	static class MultiBeanConfigWithPrimary {
		@Bean(primary = Primary.TRUE)
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean
		public TestBean serviceB() {
			return new TestBean("serviceB");
		}
	}

	@Configuration
	static class MultiBeanConfigWithMultiplePrimaries {
		@Bean(primary = Primary.TRUE)
		public TestBean serviceA() {
			return new TestBean("serviceA");
		}

		@Bean(primary = Primary.TRUE)
		public TestBean serviceB() {
			return new TestBean("serviceB");
		}
	}

	@Configuration
	static class OuterConfig {

		@Bean
		public TestBean testBean() {
			return new TestBean("outer");
		}

		@Configuration
		static class InnerConfig {

		}
	}

}
