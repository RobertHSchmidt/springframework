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

package org.springframework.config.java.proxies;

import junit.framework.TestCase;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

public class ComponentScanningTests extends TestCase {

	@Aspect
	@Component
	// TODO why is this needed?
	public static class SimpleAspect {
		@Around("execution(String *.*(..))")
		public String prependChar(ProceedingJoinPoint pjp) throws Throwable {
			String result = (String) pjp.proceed();
			return "." + result;
		}
	}

	public void testConfigurationComponentScanned() {
		ClassPathXmlApplicationContext aac = new ClassPathXmlApplicationContext(
				"/org/springframework/config/java/proxies/proxies.xml");
		// System.out.println(aac.getBeanFactory());
		aac.refresh();

		TestBean tb = (TestBean) aac.getBean("person");
		tb.setName("Rod");
		assertEquals("Aspect must fire", ".Rod", tb.getName());

		TestConfig config = (TestConfig) aac.getBean(aac.getBeanNamesForType(TestConfig.class)[0]);

		// Can get beans with direct calls on the config object
		assertSame(tb, config.person());

		assertNotNull("Configuration bean must be autowired", config.getAutoscannedObject());
		assertEquals("Aspect must fire", ".Rod", config.getAutoscannedObject().echo("Rod"));
	}
}
