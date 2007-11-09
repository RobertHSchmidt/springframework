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

/**
 *
 * @author Rod Johnson
 *
 */
public class ChainedStrategyTests extends TestCase {

	public void testNullArg() {
		try {
			new ChainedStrategy(null);
			fail();
		}
		catch (IllegalArgumentException expected) {

		}
	}

	public void testEmptyArg() {
		try {
			new ChainedStrategy(new BeanNamingStrategy[0]);
			fail();
		}
		catch (IllegalArgumentException expected) {

		}
	}

	public void testSkipsNull() {
		ChainedStrategy cs = new ChainedStrategy(new BeanNamingStrategy[] {
			new DummyNameStrategy(null),
			new DummyNameStrategy("one"),
			new DummyNameStrategy("two")
		});
		assertEquals("one", cs.getBeanName(null));
	}

	public void testReturnsFirst() {
		ChainedStrategy cs = new ChainedStrategy(new BeanNamingStrategy[] {
			new DummyNameStrategy("zero"),
			new DummyNameStrategy(null),
			new DummyNameStrategy("two")
		});
		assertEquals("zero", cs.getBeanName(null));
	}

	private class DummyNameStrategy implements BeanNamingStrategy {
		private String name;
		public DummyNameStrategy(String name) {
			this.name = name;
		}

		public String getBeanName(Method beanCreationMethod) {
			return name;
		}
	}
}
