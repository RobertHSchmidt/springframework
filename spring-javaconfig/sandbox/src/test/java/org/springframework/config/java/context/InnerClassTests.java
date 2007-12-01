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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.InnerClassTests.Outer.Inner;

public class InnerClassTests {

	@Configuration
	public static class Outer {
		@Bean
		public String outerBean() {
			return "outer";
		}

		@Configuration
		public static class Inner {
			@Bean
			public String innerBean() {
				return "inner";
			}
		}
	}

	@Test
	public void testGetContextRegistry() {
		JavaConfigApplicationContext outerCtx = new JavaConfigApplicationContext(Outer.class);
		JavaConfigApplicationContext innerCtx = outerCtx.getContextRegistry().get(Inner.class);

		assertNotNull(innerCtx);
	}

}
