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
package org.springframework.config.java.naming;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.config.java.internal.model.BeanMethod;
import org.springframework.config.java.internal.model.BeanMethodTests;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.config.java.type.Class;

public abstract class AbstractNamingStrategyTests {

	protected BeanNamingStrategy strategy;

	protected BeanMethod sampleMethod;

	protected String expectedMethodName;

	protected String expectedClassName;

	protected String expectedFqClassName;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		strategy = createNamingStrategy();
		Class sampleConfigClass = new ConfigurationClass("MyConfig").setPackage("com.acme");
		sampleMethod = BeanMethodTests.VALID_BEAN_METHOD;
		sampleMethod.setDeclaringClass(sampleConfigClass);
		expectedMethodName = sampleMethod.getName();
		expectedClassName = sampleConfigClass.getName();
		expectedFqClassName = sampleConfigClass.getFullyQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		strategy = null;
		sampleMethod = null;
	}

	protected abstract BeanNamingStrategy createNamingStrategy();

	@Test(expected = IllegalArgumentException.class)
	public void testNullMethod() {
		strategy.getBeanName((BeanMethod) null);
	}
}
