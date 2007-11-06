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

package org.springframework.config.java.testing.config;

import java.util.Arrays;

import junit.framework.TestCase;

import org.springframework.config.java.context.AnnotationApplicationContext;
import org.springframework.config.java.testing.Company;
import org.springframework.config.java.testing.SimpleConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple test which read a Spring configuration based on annotations.
 * 
 * @author Costin Leau
 * @author Rod Johnson
 * 
 */
public class SimpleAnnotationBasedConfigurationTests extends TestCase {

	public void testSimpleConfiguration() throws Exception {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("simpleConfiguration.xml", getClass());
		System.out.println(Arrays.toString(bf.getBeanDefinitionNames()));
		Company company = (Company) bf.getBean("company");
		assertEquals("Interface21", company.getName());
		assertEquals(2, company.getWorkers().size());
		assertEquals(1, company.getOwners().size());

		assertSame(bf.getBean("mark"), company.getWorkers().iterator().next());
	}

	public void testSimpleConfigurationBootstrap() throws Exception {
		new AnnotationApplicationContext(SimpleConfiguration.class.getName());
	}

	public void testAnnotationApplicationContext() throws Exception {
		AnnotationApplicationContext ctx = new AnnotationApplicationContext("org.springframework.config.java.complex");
		assertTrue("Found configurations and beans", ctx.getBeanDefinitionCount() >= 6);
	}
}
