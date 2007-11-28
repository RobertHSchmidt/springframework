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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.config.java.complex.ComplexConfiguration;
import org.springframework.config.java.testing.Company;
import org.springframework.config.java.testing.SimpleConfiguration;
import org.springframework.config.java.testing.Worker;
import org.springframework.config.java.testing.Worker.JobTitle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests that excercise the various constructors on {@link JavaConfigApplicationContext}.
 * 
 * @author Chris Beams
 */
public final class JavaConfigApplicationContextConstructionTests {

	private JavaConfigApplicationContext ctx;

	@After
	public void nullOutContext() {
		ctx = null;
	}

	@Test
	public void testBootstrappingJavaConfigViaXml() {
		ClassPathXmlApplicationContext bf = createSimpleXmlApplicationContext();
		Company company = (Company) bf.getBean("company");

		assertThat(company.getName(), equalTo("SpringSource"));
		assertThat(company.getWorkers().size(), equalTo(2));
		assertThat(company.getOwners().size(), equalTo(1));
		assertThat(company.getWorkers().iterator().next(), sameInstance(bf.getBean("mark")));
	}

	@Test
	public void testConstructionWithSingleClassAndSingleBasePackage() {
		ctx = new JavaConfigApplicationContext(new Class<?>[] { SimpleConfiguration.class },
				new String[] { "org/springframework/config/java/testing" });
		assertThat(ctx.getBeanDefinitionCount(), equalTo(5));
	}

	@Test(expected = IllegalStateException.class)
	public void testOpenEndedConstructionWithoutSettingClassesOrPackagesThrowsException() {
		ctx = new JavaConfigApplicationContext();
		ctx.refresh();
	}

	@Test
	public void testOpenEndedConstructionWithParentApplicationContextAndSingleClass() {
		ctx = new JavaConfigApplicationContext();
		ctx.setParent(createSimpleXmlApplicationContext());
		ctx.setConfigClasses(ComplexConfiguration.class);
		ctx.refresh();

		// mark comes from the xml config
		Worker mark = (Worker) ctx.getBean("mark");
		assertThat(mark.getTitle(), equalTo(JobTitle.Senior));

		// topLevelBean comes from the ComplexConfiguration class
		assertNotNull(ctx.getBean("topLevelBean"));
	}

	@Test(expected = IllegalStateException.class)
	public void testOpenEndedConstructionWithoutCallingRefreshThrowsException() {
		ctx = new JavaConfigApplicationContext();
		ctx.setParent(createSimpleXmlApplicationContext());
		ctx.setConfigClasses(ComplexConfiguration.class);

		ctx.getBean("mark"); // this should throw - we haven't called refresh
	}

	@Test(expected = IllegalStateException.class)
	public void testSetParentAfterConstructionThrowsException() {
		ctx = new JavaConfigApplicationContext(ComplexConfiguration.class, SimpleConfiguration.class);
		ctx.setConfigClasses(SimpleConfiguration.class);
		ctx.setParent(createSimpleXmlApplicationContext());
	}

	@Test
	public void testConstructionWithMultipleClassNames() {
		ctx = new JavaConfigApplicationContext(SimpleConfiguration.class, ComplexConfiguration.class);
		assertThat(ctx.getBeanDefinitionCount(), equalTo(11));
	}

	/**
	 * Sets up a base package with two classes in it, one of which is abstract and should thus be
	 * ignored
	 */
	@Test
	public void testConstructionWithSingleBasePackage() {
		String packageName = ComplexConfiguration.class.getPackage().getName();
		ctx = new JavaConfigApplicationContext(packageName);
		assertThat(ctx.getBeanDefinitionCount(), equalTo(6));
	}

	@Test
	public void testConstructionWithMultipleBasePackages() {
		String packageName1 = org.springframework.config.java.simple.EmptySimpleConfiguration.class.getPackage()
				.getName();
		String packageName2 = org.springframework.config.java.complex.ComplexConfiguration.class.getPackage().getName();
		ctx = new JavaConfigApplicationContext(packageName1, packageName2);
		assertThat(ctx.getBeanDefinitionCount(), equalTo(10));
	}

	@RunWith(Parameterized.class)
	public static class BasePackageWildcardTests {
		private final String basePackage;

		private JavaConfigApplicationContext context;

		@Parameters
		public static Collection<String[]> validWildcardValues() {
			ArrayList<String[]> values = new ArrayList<String[]>();
			values.add(new String[] { "org.springframework.config.java.complex" });
			values.add(new String[] { "org/springframework/config/java/complex" });
			values.add(new String[] { "org/springframework/*/**/comp*" });
			return values;
		}

		public BasePackageWildcardTests(String basePackage) {
			this.basePackage = basePackage;
		}

		@Test
		public void testConstructionWithWildcardBasePackage() {
			context = new JavaConfigApplicationContext(basePackage);

			assertThat(context.getBeanDefinitionCount(), equalTo(6));
		}
	}

	@Test
	public void testConstructionWithMixOfClassesAndBasePackages() {
		String pkg1 = org.springframework.config.java.simple.EmptySimpleConfiguration.class.getPackage().getName();
		ctx = new JavaConfigApplicationContext(new Class<?>[] { ComplexConfiguration.class }, new String[] { pkg1 });

		assertThat(ctx.getBeanDefinitionCount(), equalTo(10));
	}

	private ClassPathXmlApplicationContext createSimpleXmlApplicationContext() {
		return new ClassPathXmlApplicationContext("simpleConfiguration.xml", getClass());
	}

}
