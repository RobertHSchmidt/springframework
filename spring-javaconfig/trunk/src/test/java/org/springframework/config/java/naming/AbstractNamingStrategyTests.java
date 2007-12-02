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
package org.springframework.config.java.naming;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public abstract class AbstractNamingStrategyTests extends TestCase {

	protected BeanNamingStrategy strategy;

	protected Method sampleMethod;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		strategy = createNamingStrategy();
		sampleMethod = AbstractNamingStrategyTests.class.getDeclaredMethod("setUp", (Class[]) null);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		strategy = null;
		sampleMethod = null;
	}

	protected abstract BeanNamingStrategy createNamingStrategy();

	public void testNullMethod() {
		try {
			strategy.getBeanName(null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}
}
