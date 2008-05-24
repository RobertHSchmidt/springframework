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

import static org.junit.Assert.*;
import static org.springframework.config.java.test.Assert.assertBeanDefinitionCount;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.config.java.ConfigurationPostProcessorTests.ExternalBeanConfiguration;
import org.springframework.config.java.ConfigurationPostProcessorTests.ExternalBeanProvidingConfiguration;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.complex.AbstractConfigurationToIgnore;
import org.springframework.config.java.complex.ComplexConfiguration;
import org.springframework.config.java.model.ValidationError;
import org.springframework.config.java.process.MalformedJavaConfigurationException;
import org.springframework.config.java.simple.SimpleConfigurationWithOneBean;
import org.springframework.config.java.support.ConfigurationSupport;

/**
 * @author Costin Leau
 * @author Rod Johnson
 * @author Chris Beams
 */
public final class JavaConfigApplicationContextTests {

	protected JavaConfigApplicationContext ctx;

	@After
	public void tearDown() {
		try {
			// just in case some test fails during loading and refresh is not
			// called
			ctx.refresh();
			ctx.close();
		}
		catch (Exception ex) {
			// ignore - it's cleanup time
		}
		ctx = null;
	}

	static class SimpleConfig { public @Bean TestBean alice() { return new TestBean("alice"); } }
	public @Test void singleClass() {
		ctx = new JavaConfigApplicationContext(SimpleConfig.class);

		TestBean alice = ctx.getBean(TestBean.class);

		assertNotNull(alice);
		assertEquals("alice", alice.getName());
		// prove singleton semantics are respected
		assertSame(alice, ctx.getBean(TestBean.class));
	}

	@Test
	public void testReadSimplePackage() {
		ctx = new JavaConfigApplicationContext("/org/springframework/config/java/simple");

		int classesInPackage = 2;
		int beansInClasses = 3;

		assertBeanDefinitionCount(ctx, (classesInPackage + beansInClasses));
	}

	/**
	 * complex.* package contains only one top-level configuration class. Inner
	 * classes should not be included.
	 */
	@Test
	public void testScanningPackageDoesNotIncludeInnerConfigurationClasses() {
		ctx = new JavaConfigApplicationContext("/org/springframework/config/java/complex");

		assertBeanDefinitionCount(ctx, 2);
	}

	@Test
	public void testReadClassesByName() {
		ctx = new JavaConfigApplicationContext(ComplexConfiguration.class, SimpleConfigurationWithOneBean.class);

		int classesInPackage = 2;
		int beansInClasses = 2;

		assertBeanDefinitionCount(ctx, (classesInPackage + beansInClasses));
	}

	// ------------------------------------------------------------------------

	@Test
	public void testProcessImports() {
		ctx = new JavaConfigApplicationContext(ConfigurationWithImportAnnotation.class);

		int configClasses = 2;
		int beansInClasses = 2;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	@Import(OtherConfiguration.class)
	@Configuration
	static class ConfigurationWithImportAnnotation {
		@Bean
		public ITestBean one() {
			return new TestBean();
		}
	}

	@Configuration
	static class OtherConfiguration {
		@Bean
		public ITestBean two() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testImportAnnotationWithTwoLevelRecursion() {
		ctx = new JavaConfigApplicationContext(AppConfig.class);

		int configClasses = 2;
		int beansInClasses = 3;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	@Import(DataSourceConfig.class)
	@Configuration
	static class AppConfig extends ConfigurationSupport {
		@Bean
		public ITestBean transferService() {
			return new TestBean(accountRepository());
		}

		@Bean
		public ITestBean accountRepository() {
			return new TestBean((ITestBean) this.getBean("dataSourceA"));
		}
	}

	@Configuration
	static class DataSourceConfig {
		@Bean
		public ITestBean dataSourceA() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testImportAnnotationWithThreeLevelRecursion() {
		ctx = new JavaConfigApplicationContext(FirstLevel.class);

		int configClasses = 3;
		int beansInClasses = 5;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	@Import(SecondLevel.class)
	@Configuration
	static class FirstLevel { public @Bean TestBean m() { return new TestBean(); } }

	@Import(ThirdLevel.class)
	@Configuration
	static class SecondLevel { public @Bean TestBean n() { return new TestBean(); } }

	@Configuration
	static class ThirdLevel {
		@Bean
		public ITestBean thirdLevelA() {
			return new TestBean();
		}

		@Bean
		public ITestBean thirdLevelB() {
			return new TestBean();
		}

		@Bean
		public ITestBean thirdLevelC() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testImportAnnotationWithMultipleArguments() {
		ctx = new JavaConfigApplicationContext(WithMultipleArgumentsToImportAnnotation.class);

		int configClasses = 3;
		int beansInClasses = 3;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	@Import( { LeftConfig.class, RightConfig.class })
	@Configuration
	static class WithMultipleArgumentsToImportAnnotation { public @Bean TestBean m() { return new TestBean(); } }

	@Configuration
	static class LeftConfig { public @Bean ITestBean left() { return new TestBean(); } }

	@Configuration
	static class RightConfig { public @Bean ITestBean right() { return new TestBean(); }
	}

	// ------------------------------------------------------------------------

	@Test
	public void testImportAnnotationWithMultipleArgumentsResultingInDuplicateBeanDefinition() {
		boolean threw = false;
		try {
			ctx = new JavaConfigApplicationContext(WithMultipleArgumentsThatWillCauseDuplication.class);
		}
		catch (MalformedJavaConfigurationException ex) {
			assertTrue(ex.getMessage().contains(ValidationError.ILLEGAL_BEAN_OVERRIDE.toString()));
			threw = true;
		}
		assertTrue("Did not detect duplication and throw as expected", threw);
	}

	@Import({ Foo1.class, Foo2.class })
	@Configuration
	static class WithMultipleArgumentsThatWillCauseDuplication { }

	@Configuration
	static class Foo1 {
		@Bean(allowOverriding = false)
		public ITestBean foo() {
			return new TestBean();
		}
	}

	@Configuration
	static class Foo2 {
		@Bean
		public ITestBean foo() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	@Test
	public void testImportAnnotationOnInnerClasses() {
		ctx = new JavaConfigApplicationContext(OuterConfig.InnerConfig.class);

		int configClasses = 2;
		int beansInClasses = 2;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	static class ExternalConfig { public @Bean ITestBean extBean() { return new TestBean(); } }
	static class OuterConfig {
		@Bean String whatev() { return "whatev"; }
		@Import(ExternalConfig.class)
		static class InnerConfig { public @Bean ITestBean innerBean() { return new TestBean(); } }
	}

	// ------------------------------------------------------------------------

	@Test(expected=MalformedJavaConfigurationException.class)
	public void testAbstractConfigurationDoesNotGetProcessed() {
		ctx = new JavaConfigApplicationContext(AbstractConfigurationToIgnore.class);
	}

	@Test
	public void testAbstractConfigurationWithExternalBeanDoesGetProcessed() {
		ctx = new JavaConfigApplicationContext(ExternalBeanConfiguration.class,
				ExternalBeanProvidingConfiguration.class);

		int configClasses = 2;
		int beansInClasses = 2;

		assertBeanDefinitionCount(ctx, (configClasses + beansInClasses));
	}

	// ------------------------------------------------------------------------

	@Test
	public void testOpenEndedConstructionSupplyingBasePackagesBeforeRefresh() {
		ctx = new JavaConfigApplicationContext();
		ctx.setBasePackages("org.springframework.config.java.context.forscanning.a");
		ctx.refresh();

		assertBeanDefinitionCount(ctx, 2);
	}

	// TODO: complete this testing
	@Ignore
	@Test
	public void testAllVariationsOnOrderingOfClassesAndBasePackages() {
		fail("not yet implemented");
	}

	// TODO: if two classes share a common outer class, this shouldn't cause a problem.

	// TODO: PotentialConfigurationClass is not yet doing customized validation

	// TODO: Declaring classes are not yet considered during validation (otherwise they would fail in cases such as this one)

	// TODO: what about abstract outer classes?

	public static class NameConfig { public @Bean String name() { return "lewis"; } }
	@Import(NameConfig.class)
	public abstract static class Outer {
		public @Bean TestBean foo() { return new TestBean("foo"); }
		public @Bean TestBean bar() { return new TestBean(name()); }
		abstract @ExternalBean String name();
		static class Other { @Bean TestBean alice() { return new TestBean("alice"); } }
	}

	@Import({Outer.Other.class})
	public static class Config { }

	public static class DeclaringClass {
		public @Bean TestBean outer() { return new TestBean(); }
		public static class MemberClass { public @Bean TestBean inner() { return new TestBean(); } }
	}

	// TODO: rename
	public @Test void simplestPossibleRepro() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(DeclaringClass.MemberClass.class);
		ctx.getBean("inner");
		ctx.getBean("outer");
	}

	// TODO: rename
	public @Test void main() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config.class);
		String name = ctx.getBean(String.class, "name");
		//ctx.getParent().getBean("foo");
		ctx.getBean("foo");
		TestBean bar = ctx.getBean(TestBean.class, "bar");
		assertEquals("lewis", bar.getName());
	}

	static class Child { public @Bean TestBean child() { return new TestBean("alice"); } }
	static class Parent { public @Bean TestBean parent() { return new TestBean("mother"); } }
	// TODO: rename
	public @Test void simple() {
		JavaConfigApplicationContext c = new JavaConfigApplicationContext();
		c.addConfigClasses(Child.class);
		JavaConfigApplicationContext p = new JavaConfigApplicationContext(Parent.class);
		c.setParent(p);
		c.refresh();
		assertEquals("alice", c.getBean(TestBean.class, "child").getName());
		assertEquals("mother", p.getBean(TestBean.class, "parent").getName());
		assertEquals("mother", c.getBean(TestBean.class, "parent").getName());
	}

	// TODO: rename
	public @Test void simple2() {
		final JavaConfigApplicationContext p = new JavaConfigApplicationContext(Parent.class);
		assertEquals("mother", p.getBean(TestBean.class, "parent").getName());

		JavaConfigApplicationContext c = new JavaConfigApplicationContext();
		c.addConfigClasses(Child.class);
		c.setParent(p);
		c.refresh();
		assertEquals("alice", c.getBean(TestBean.class, "child").getName());
		assertEquals("mother", ((TestBean)c.getParent().getBean("parent")).getName());
		assertEquals("mother", ((TestBean)c.getBean("parent")).getName());
	}


	// ------------------------------------------------------------------------

	// TODO: allow for a class that a) does not declare @Configuration, b) does
	// not declare any methods annotated with @Bean, and c) DOES have a valid
	// @Import annotation. (right now this configuration wouldn't work because
	// ProcessUtils.isConfigurationClass() would fail)

	// TODO: in testImportAnnotationWithTwoLevelRecursion(), if the
	// DataSourceConfig changes it's dataSourceA() bean to dataSource() and
	// callers are updated appropriately, a test way over in HibernateTests
	// fails (testStoreEntity()). There's some kind of shared state issue going
	// on, and it took quite a while to track down this far. This should be
	// checked out, but doesn't appear high-pri at this point, nor does it seem
	// to be caused by the new support for @Import

}
