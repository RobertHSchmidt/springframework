/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.beans.factory.java;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.annotation.Lazy;
import org.springframework.beans.factory.support.AbstractClassScanningBeanDefinitionReader;
import org.springframework.beans.factory.support.ConfigurationClassScanningBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Costin Leau
 * 
 */
public class ConfigurationScanningBeanDefinitionReaderTests extends TestCase {

	@Configuration
	private static class SimpleConfiguration {
	}

	private class ComplexConfiguration {
		@Transactional(propagation = Propagation.MANDATORY)
		@Configuration(defaultLazy = Lazy.UNSPECIFIED, defaultAutowire = Autowire.INHERITED, useFactoryAspects = false)
		private class InnerConfiguration {
		}

		@Configuration
		private class DeepConfiguration {
			@Configuration(names = { "foo", "bar" })
			private class VeryDeepConfiguration {
				public void anonymousClass() {
					new InnerConfiguration() {
					};
				}
			}
		}
	}

	protected AbstractClassScanningBeanDefinitionReader reader;

	protected GenericApplicationContext registry;

	protected void setUp() throws Exception {
		super.setUp();
		registry = new GenericApplicationContext();
		reader = new ConfigurationClassScanningBeanDefinitionReader(registry);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		reader = null;
		try {
			// just in case some test fails during loading and refresh is not
			// called
			registry.refresh();
			registry.close();
		}
		catch (Exception e) {
			// ignore - it's cleanup time
		}
		registry = null;
	}

	public void testReadClass() throws Exception {
		assertEquals(
				3,
				reader.loadBeanDefinitions("/org/springframework/beans/factory/java/ConfigurationScanningBeanDefinitionReaderTests$ComplexConfiguration.class"));
		// registry.refresh();
	}

	public void testReadInnerClass() throws Exception {
		assertEquals(
				4,
				reader.loadBeanDefinitions("/org/springframework/beans/factory/java/ConfigurationScanningBeanDefinitionReaderTests.class"));
	}

	public void tstReadClasses() throws Exception {
		assertEquals(
				4,
				reader.loadBeanDefinitions("/org/springframework/beans/factory/**/ConfigurationScanningBeanDefinitionReaderTests.class"));
	}

	public void testReadClassByName() throws Exception {
		assertEquals(4, reader.loadBeanDefinitions(new String[] {
				"org.springframework.beans.factory.java.ConfigurationScanningBeanDefinitionReaderTests$ComplexConfiguration",
				"org.springframework.beans.factory.java.ConfigurationScanningBeanDefinitionReaderTests$SimpleConfiguration" }));
	}

}
