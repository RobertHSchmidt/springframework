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
package org.springframework.config.java.context.web;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.complex.ComplexConfiguration;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

public class JavaConfigWebApplicationContextTests {

	private JavaConfigWebApplicationContext ctx;

	private final String class1 = MyConfig.class.getName();

	private final String[] classes = new String[] { class1 };

	@Before
	public void initContext() {
		ctx = new JavaConfigWebApplicationContext();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetConfigLocationsWithEmptyArray() {
		ctx.setConfigLocations(new String[] {});
		ctx.refresh();
	}

	@Test(expected = NullPointerException.class)
	public void testSetConfigLocationsWithNullArray() {
		ctx.setConfigLocations(null);
	}

	@Test(expected = NullPointerException.class)
	public void testSetConfigLocationsWithArrayContainingNullElement() {
		ctx.setConfigLocations(new String[] { class1, null });
	}

	@Test
	// (expected = IllegalArgumentException.class)
	// unfortunately, we can't expect to throw an exception on a bogus class,
	// because the parsing
	// algorithm assumes that something that is not a class must be a base
	// package pattern
	// TODO: revisit this - can we make a clear distinction between classes and
	// packages? would be
	// nice to be able to expect ClassNotFoundException /
	// IllegalArgumentException when a user typos
	// a class. suggestion: check location for '/' or '*' - this guarantees that
	// it's intended to be
	// a base package. If there are no wildcards provided, then try to
	// instantiate a package via
	// Package.getPackage(location); if that returns null, then try
	// Class.forName(location). If that
	// throws ClassNotFoundException, then throw new
	// IllegalArgumentException("location could not be
	// resolved to a class or package on the classpath");
	public void testSetConfigLocationsWithArrayContainingBogusClassName() {
		ctx.setConfigLocations(new String[] { "com.foo.NotExist" });
		ctx.refresh();

		assertThat(ctx.getBeanDefinitionCount(), equalTo(0));
	}

	/**
	 * Tests that a JavaConfigWebApplicationContext must have refresh() called
	 * on it before being used. refresh() is called as part of the lifecycle of
	 * {@link DispatcherServlet} and {@link ContextLoaderListener}. Typically
	 * users would not manually instantiate this class.
	 */
	@Test(expected = IllegalStateException.class)
	public void testManualRefreshRequired() {
		ctx.setConfigLocations(classes);
		ctx.getBean("foo"); // this will throw, refresh has not been called
	}

	@Test
	public void testSetConfigLocationsWithSingleClass() {
		ctx.setConfigLocations(classes);
		ctx.refresh();

		assertThat(ctx.getBeanDefinitionCount(), equalTo(2));
	}

	@Test
	public void testSetConfigLocationsWithDuplicateClass() {
		ctx.setConfigLocations(new String[] { class1, class1 });
		ctx.refresh();

		assertThat(ctx.getBeanDefinitionCount(), equalTo(2));
	}

	@Test
	public void testConstructionWithMixOfClassesAndBasePackages() {
		String pkg1 = org.springframework.config.java.simple.EmptySimpleConfiguration.class.getPackage().getName();

		ctx.setConfigLocations(new String[] { ComplexConfiguration.class.getName(), pkg1 });
		ctx.refresh();

		assertThat(ctx.getBeanDefinitionCount(), equalTo(6));
	}

	@Test
	public void testConstructionWithWildcardBasePackage() {
		// *ple* pattern matches both 'simple' and 'complex'
		ctx.setConfigLocations(new String[] { "org.springframework.config.java.*ple*" });
		ctx.refresh();

		assertThat(ctx.getBeanDefinitionCount(), equalTo(6));
	}

	@Configuration
	static class MyConfig {
		@Bean
		public String foo() {
			return "foo1";
		}
	}

}
