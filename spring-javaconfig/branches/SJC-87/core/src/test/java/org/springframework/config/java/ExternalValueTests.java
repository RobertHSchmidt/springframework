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
package org.springframework.config.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * Test for properties resolution
 *
 * @author Rod Johnson
 * @author Chris Beams
 */
public class ExternalValueTests {

	/**
	 * Test fixture: each test method must initialize
	 */
	private ConfigurableJavaConfigApplicationContext ctx;

	/**
	 * It is up to each individual test to initialize the context;
	 * null it out before each subsequent test just to be safe
	 */
	@After
	public void nullOutContext() { ctx = null; }


	// XXX: [@ExternalValue]
	public @Test void testStringAndBooleanProperty() throws Exception {
		ctx = new JavaConfigApplicationContext(AbstractConfigurationDependsOnProperties.class);
		TestBean rod = ctx.getBean(TestBean.class, "rod");
		assertEquals("String property must be resolved correctly", "Rod", rod.getName());
		assertTrue("Boolean property must be resolved correctly", rod.isJedi());
	}
	// XXX: [@ExternalValue]
	public @Test void testIntProperty() throws Exception {
		ctx = new JavaConfigApplicationContext(AbstractConfigurationDependsOnProperties.class);
		TestBean rod = ctx.getBean(TestBean.class, "rod");
		assertEquals("int property must be resolved correctly", 37, rod.getAge());
	}
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class AbstractConfigurationDependsOnProperties {
		public @Bean TestBean rod() {
			TestBean rod = new TestBean();
			rod.setName(getName());
			rod.setAge(ignoreThisNameDueToAnnotationValue());

			rod.setJedi(jedi());
			return rod;
		}

		public abstract @ExternalValue String getName();
		public abstract @ExternalValue("age") int ignoreThisNameDueToAnnotationValue();
		protected abstract @ExternalValue boolean jedi();
	}


	// XXX: [@ExternalValue]
	public @Test void testDefaultValueInImplementationBody() throws Exception {
		ctx = new JavaConfigApplicationContext(DefaultValuesConfig.class);
		TestBean rod = ctx.getBean(TestBean.class, "rod");
		assertEquals("int property must default correctly if there's a concrete method", 25, rod.getAge());
	}
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class DefaultValuesConfig {
		public @Bean TestBean rod() {
			TestBean rod = new TestBean();
			rod.setName(getName());
			rod.setAge(otherNumber());
			return rod;
		}
		public abstract @ExternalValue String getName();
		public @ExternalValue int otherNumber() { return 25; }
	}


	// XXX: [@ExternalValue]
	@Test(expected = BeanCreationException.class)
	public void testUnresolved() throws Exception {
		// unresolvedName will cause an exception
		ctx = new JavaConfigApplicationContext(MissingValuesConfig.class);
	}
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class MissingValuesConfig {
		public @Bean TestBean rod() { return new TestBean(unresolvedName()); }
		public abstract @ExternalValue String unresolvedName();
	}

}