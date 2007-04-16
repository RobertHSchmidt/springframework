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
package org.springframework.config.java.process.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;

public class BeanAnnotationNameStrategyTests extends AbstractNamingStrategyTests {

	@Override
	protected BeanNamingStrategy createNamingStrategy() {
		return new BeanAnnotationNameStrategy();
	}


	@Bean()
	public void noAnnotation() {
	};

	@Bean()
	public void noName() {
	};

	@Bean(name = "everlastingGaze")
	public void nameSpecified() {
	};

	public void testNoName() throws Exception {
		Method method = getClass().getMethod("noAnnotation", null);
		assertNull(strategy.getBeanName(method, null));
	}

	public void testDefaultName() throws Exception {
		Method method = getClass().getMethod("noName", null);
		assertNull(strategy.getBeanName(method, null));
	}

	public void testNameSpecified() throws Exception {
		Method method = getClass().getMethod("nameSpecified", null);
		assertEquals("everlastingGaze", strategy.getBeanName(method, null));
	}

}
