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

package org.springframework.config.java.context;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.config.java.ConfigurationPostProcessorTests.ExternalBeanConfiguration;
import org.springframework.config.java.ConfigurationPostProcessorTests.ExternalBeanProvidingConfiguration;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.complex.AbstractConfigurationToIgnore;
import org.springframework.config.java.complex.ComplexConfiguration;
import org.springframework.config.java.simple.EmptySimpleConfiguration;
import org.springframework.config.java.support.ConfigurationSupport;

/**
 * @author Costin Leau
 * @author Rod Johnson
 * @author Chris Beams
 */
public final class JavaConfigApplicationContextTests extends TestCase {

	protected JavaConfigApplicationContext ctx;

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ctx = null;
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

	public void testReadSimplePackage() throws Exception {
		ctx = new JavaConfigApplicationContext("/org/springframework/config/java/simple");

		int classesInPackage = 2;
		int beansInClasses = 2;

		assertEquals(classesInPackage + beansInClasses, ctx.getBeanDefinitionCount());
	}

	public void testReadInnerClassesInPackage() throws Exception {
		ctx = new JavaConfigApplicationContext("/org/springframework/config/java/complex");

		assertEquals(6, ctx.getBeanDefinitionCount());
	}

	public void testReadClassesByName() throws Exception {
		ctx = new JavaConfigApplicationContext(ComplexConfiguration.class, EmptySimpleConfiguration.class);

		int classesInPackage = 4;
		int beansInClasses = 3;

		assertEquals(classesInPackage + beansInClasses, ctx.getBeanDefinitionCount());
	}

	// ------------------------------------------------------------------------

	public void testProcessImports() {
		ctx = new JavaConfigApplicationContext(ConfigurationWithImportAnnotation.class);

		int configClasses = 2;
		int beansInClasses = 2;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
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

	public void testImportAnnotationWithTwoLevelRecursion() {
		ctx = new JavaConfigApplicationContext(AppConfig.class);

		int configClasses = 2;
		int beansInClasses = 3;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
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

	public void testImportAnnotationWithThreeLevelRecursion() {
		ctx = new JavaConfigApplicationContext(FirstLevel.class);

		int configClasses = 3;
		int beansInClasses = 3;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
	}

	@Import(SecondLevel.class)
	@Configuration
	static class FirstLevel {
	}

	@Import(ThirdLevel.class)
	@Configuration
	static class SecondLevel {
	}

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

	public void testImportAnnotationWithMultipleArguments() {
		ctx = new JavaConfigApplicationContext(WithMultipleArgumentsToImportAnnotation.class);

		int configClasses = 3;
		int beansInClasses = 2;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
	}

	@Import( { LeftConfig.class, RightConfig.class })
	@Configuration
	static class WithMultipleArgumentsToImportAnnotation {
	}

	@Configuration
	static class LeftConfig {
		@Bean
		public ITestBean left() {
			return new TestBean();
		}
	}

	@Configuration
	static class RightConfig {
		@Bean
		public ITestBean right() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	public void testImportAnnotationWithMultipleArgumentsResultingInDuplicateBeanDefinition() {
		boolean threw = false;
		try {
			ctx = new JavaConfigApplicationContext(WithMultipleArgumentsThatWillCauseDuplication.class);
		}
		catch (IllegalStateException e) {
			threw = true;
		}
		assertTrue("Did not detect duplication and throw as expected", threw);
	}

	@Import( { Foo1.class, Foo2.class })
	@Configuration
	static class WithMultipleArgumentsThatWillCauseDuplication {
	}

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

	public void testImportAnnotationOnInnerClasses() {
		ctx = new JavaConfigApplicationContext(OuterConfig.class);

		int configClasses = 3;
		int beansInClasses = 2;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
	}

	@Configuration
	static class OuterConfig {
		@Import(ExternalConfig.class)
		@Configuration
		static class InnerConfig {
			@Bean
			public ITestBean innerBean() {
				return new TestBean();
			}
		}
	}

	@Configuration
	static class ExternalConfig {
		@Bean
		public ITestBean extBean() {
			return new TestBean();
		}
	}

	// ------------------------------------------------------------------------

	public void testAbstractConfigurationDoesNotGetProcessed() {
		ctx = new JavaConfigApplicationContext(AbstractConfigurationToIgnore.class);

		int configClasses = 0;
		int beansInClasses = 0;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
	}

	public void testAbstrectConfigurationWithExternalBeanDoesGetProcessed() {
		ctx = new JavaConfigApplicationContext(ExternalBeanConfiguration.class,
				ExternalBeanProvidingConfiguration.class);

		int configClasses = 2;
		int beansInClasses = 2;

		assertEquals(configClasses + beansInClasses, ctx.getBeanDefinitionCount());
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
