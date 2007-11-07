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

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Import;
import org.springframework.config.java.listener.registry.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.support.ConfigurationSupport;

/**
 * Tests for using the &#64;Import annotation feature. Modeled after the tests
 * in {@link ConfigurationProcessorTests}.
 * 
 * @see Import
 * @see ConfigurationProcessor
 * @see ConfigurationProcessorTests
 * @author Chris Beams
 */
public class ConfigurationProcessorImportAnnotationTests extends TestCase {

	{
		new DefaultConfigurationListenerRegistry();
	}

	public void testImportAnnotationWithTwoLevelRecursion() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		int nBeanDefsGenerated = configurationProcessor.processClass(AppConfig.class);
		ITestBean transferService = (ITestBean) bf.getBean("transferService");
		ITestBean accountRepository = (ITestBean) bf.getBean("accountRepository");
		ITestBean dataSourceA = (ITestBean) bf.getBean("dataSourceA");
		assertNotNull(transferService);
		assertNotNull(accountRepository);
		assertNotNull(dataSourceA);
		assertEquals(2 + 3, nBeanDefsGenerated);
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
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		int nBeanDefsGenerated = configurationProcessor.processClass(FirstLevel.class);
		ITestBean tb3a = (ITestBean) bf.getBean("thirdLevelA");
		ITestBean tb3b = (ITestBean) bf.getBean("thirdLevelB");
		ITestBean tb3c = (ITestBean) bf.getBean("thirdLevelC");
		assertNotNull(tb3a);
		assertNotNull(tb3b);
		assertNotNull(tb3c);
		assertEquals(3 + 3, nBeanDefsGenerated);
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
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		int nBeanDefsGenerated = configurationProcessor.processClass(WithMultipleArgumentsToImportAnnotation.class);
		ITestBean left = (ITestBean) bf.getBean("left");
		ITestBean right = (ITestBean) bf.getBean("right");
		assertNotNull(left);
		assertNotNull(right);
		assertEquals(3 + 2, nBeanDefsGenerated);
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
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		boolean threw = false;
		try {
			configurationProcessor.processClass(WithMultipleArgumentsThatWillCauseDuplication.class);
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
		@Bean
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