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

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.AspectJConfigurationProcessorTests.CountingConfiguration;
import org.springframework.config.java.AspectJConfigurationProcessorTests.SingletonCountingAdvice;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.ConfigurationProcessor;

/**
 * @author Rod Johnson
 * @author Chris Beams
 */
public class SpringAopConfigurationProcessorTests {

	@Test
	public void testPerInstanceAdviceAndSharedAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(SpringAroundPerInstanceAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		Object target1 = ((Advised) advised1).getTargetSource().getTarget();
		TestBean advised2 = (TestBean) bf.getBean("advised");

		// Hashcode works on this
		advised2.setAge(35);

		assertNotSame(advised1, advised2);
		Object target2 = ((Advised) advised2).getTargetSource().getTarget();
		assertNotSame(target1, target2);

		assertEquals("advised", bf.getBeanNamesForType(TestBean.class)[0]);

		assertEquals(0, CountingConfiguration.getCount(target1));
		advised1.absquatulate();
		assertEquals(0, CountingConfiguration.getCount(target1));
		advised1.getSpouse();
		assertEquals(1, CountingConfiguration.getCount(target1));
		assertEquals(0, CountingConfiguration.getCount(target2));

		advised2.getSpouse();
		assertEquals(1, CountingConfiguration.getCount(target1));
		assertEquals(1, CountingConfiguration.getCount(target2));
	}

	@Test
	public void testAroundAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(SpringAroundPerInstanceAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		assertTrue("Object was not proxied as expected", AopUtils.isAopProxy(advised1));
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}

	@Test
	public void testNoAroundAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		// Superclass doesn't have around advice
		configurationProcessor.processClass(SingletonCountingAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("tony", advised1.getName());
	}

	@Configuration
	@Aspect
	public static class SpringAroundPerInstanceAdvice extends CountingConfiguration {

		@Before("execution(* getSpouse()) && target(target)")
		public void doesntMatter(Object target) {
			Integer count = counts.get(target);
			if (count == null) {
				count = 0;
			}
			++count;
			counts.put(target, count);
		}

		@Around("execution(* *.getName())")
		public Object around() {
			return "around";
		}
	}

	public static class InterceptAllAdvice {
		public static int count;

		@Before("* *(..)")
		protected void count() {
			++count;
		}

		@Bean
		public TestBean kaare() {
			return new TestBean();
		}
	}

	// FIXME: interception on self causes circular dependency - can this be
	// prevented?
	@Ignore
	@Test
	public void testInterceptAll() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(InterceptAllAdvice.class);

		TestBean kaare = (TestBean) bf.getBean("kaare");

		// Can't start at 0 because of factory methods such as setBeanFactory()
		int invocations = InterceptAllAdvice.count = 0;

		kaare.absquatulate();
		assertEquals(++invocations, InterceptAllAdvice.count);
		kaare.getAge();
		assertEquals(++invocations, InterceptAllAdvice.count);
	}

}
