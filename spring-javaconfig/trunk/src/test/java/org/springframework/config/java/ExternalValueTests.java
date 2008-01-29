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

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.process.ConfigurationProcessor;

/**
 * Test for properties resolution
 * 
 * @author Rod Johnson
 * 
 */
public class ExternalValueTests extends TestCase {

	@Configuration
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class AbstractConfigurationDependsOnProperties {
		@Bean
		public TestBean rod() {
			TestBean rod = new TestBean();
			rod.setName(getName());
			rod.setAge(ignoreThisNameDueToAnnotationValue());

			rod.setJedi(jedi());
			return rod;
		}

		@ExternalValue
		public abstract String getName();

		@ExternalValue("age")
		public abstract int ignoreThisNameDueToAnnotationValue();

		@ExternalValue
		protected abstract boolean jedi();

	}

	@Configuration
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class DefaultValues {
		@Bean
		public TestBean rod() {
			TestBean rod = new TestBean();
			rod.setName(getName());
			rod.setAge(otherNumber());
			return rod;
		}

		@ExternalValue
		public abstract String getName();

		@ExternalValue
		public int otherNumber() {
			return 25;
		}

	}

	@Configuration
	@ResourceBundles("classpath:/org/springframework/config/java/simple")
	static abstract class MissingValues {
		@Bean
		public TestBean rod() {
			TestBean rod = new TestBean();
			rod.setName(unresolved());
			return rod;
		}

		@ExternalValue
		public abstract String unresolved();

	}

	public void testStringAndBooleanProperty() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(AbstractConfigurationDependsOnProperties.class);
		TestBean rod = (TestBean) bf.getBean("rod");
		assertEquals("String property must be resolved correctly", "Rod", rod.getName());
		assertTrue("Boolean property must be resolved correctly", rod.isJedi());
	}

	public void testIntProperty() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(AbstractConfigurationDependsOnProperties.class);
		TestBean rod = (TestBean) bf.getBean("rod");
		assertEquals("int property must be resolved correctly", 37, rod.getAge());
	}

	public void testDefaultValueInImplementationBody() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(DefaultValues.class);
		TestBean rod = (TestBean) bf.getBean("rod");
		assertEquals("int property must default correctly if there's a concrete method", 25, rod.getAge());
	}

	/**
	 * @throws Exception
	 */
	public void testUnresolved() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		try {
			configurationProcessor.processClass(MissingValues.class);
			bf.getBean("rod");
			// TODO do we want to fail earlier than this?

			fail();
		}
		catch (BeanCreationException ex) {
			// Ok
		}
	}

}
