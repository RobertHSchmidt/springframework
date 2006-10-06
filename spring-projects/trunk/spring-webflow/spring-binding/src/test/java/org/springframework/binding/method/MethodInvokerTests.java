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
package org.springframework.binding.method;

import junit.framework.TestCase;

/**
 * Unit tests for {@link org.springframework.binding.method.MethodInvoker}.
 * 
 * @author Erwin Vervaet
 */
public class MethodInvokerTests extends TestCase {
	
	private MethodInvoker methodInvoker;
	
	protected void setUp() throws Exception {
		this.methodInvoker = new MethodInvoker();
	}
	
	public void testInvocationTargetException() {
		try {
			methodInvoker.invoke(new MethodSignature("test"), new TestObject(), null);
			fail();
		}
		catch (MethodInvocationException e) {
			assertTrue(e.getTargetException() instanceof IllegalArgumentException);
			assertEquals("just testing", e.getTargetException().getMessage());
		}
	}

	private static class TestObject {
		
		public void test() {
			throw new IllegalArgumentException("just testing");
		}
	}
}
