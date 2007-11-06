/*
 * Copyright 2002-2004 the original author or authors.
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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.AspectJConfigurationProcessorTests.CountingConfiguration;
import org.springframework.config.java.AspectJConfigurationProcessorTests.SingletonCountingAdvice;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.SpringAdvice;
import org.springframework.config.java.listener.registry.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.core.annotation.Order;

/**
 * 
 * @author Rod Johnson
 */
public class SpringAopConfigurationProcessorTests extends TestCase {

	{
		new DefaultConfigurationListenerRegistry();
	}

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
		// System.out.println("t1=" + System.identityHashCode(target1) + "; t2="
		// + System.identityHashCode(target2));

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

	public void testAroundAdvice() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		configurationProcessor.processClass(SpringAroundPerInstanceAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		int newAge = 24;
		advised1.setAge(newAge);
		assertEquals("Invocations must work on target without around advice", newAge, advised1.getAge());
		assertEquals("around", advised1.getName());
	}

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

	public void testCanControlInterceptorPosition() throws Exception {
		OrderCheckingInterceptor.clear();

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);
		// Superclass doesn't have around advice
		configurationProcessor.processClass(OrderedSpringAdvice.class);

		TestBean advised1 = (TestBean) bf.getBean("advised");
		assertNull(advised1.getSpouse());
	}

	public static class SpringAroundPerInstanceAdvice extends CountingConfiguration {

		@Bean
		@SpringAdvice("execution(* getSpouse())")
		public MethodBeforeAdvice doesntMatter() {
			return new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) throws Throwable {
					// System.out.println("Advised on Targetid=" +
					// System.identityHashCode(target));
					Integer count = counts.get(target);
					if (count == null) {
						count = 0;
					}
					++count;
					counts.put(target, count);
				}
			};
		}

		@Bean
		@SpringAdvice("execution(* *.getName())")
		public MethodInterceptor around() {
			return new MethodInterceptor() {
				public Object invoke(MethodInvocation mi) throws Throwable {
					return "around";
				}
			};
		}
	}

	public static class OrderedSpringAdvice extends CountingConfiguration {

		@Bean
		@Order(0)
		@SpringAdvice("execution(* getSpouse())")
		public MethodInterceptor first() {
			return new OrderCheckingInterceptor(0);
		}

		@Bean
		@Order(1)
		@SpringAdvice("execution(* getSpouse())")
		public MethodInterceptor second() {
			return new OrderCheckingInterceptor(1);
		}

		@Bean()
		@Order(2)
		@SpringAdvice("execution(* getSpouse())")
		protected MethodInterceptor third() {
			return new OrderCheckingInterceptor(2);
		}
	}

	private static class OrderCheckingInterceptor implements MethodInterceptor {
		private static ThreadLocal<Integer> expectedInterceptorPosition = new ThreadLocal<Integer>();

		public static void clear() {
			expectedInterceptorPosition.set(0);
		}

		private int getPosition() {
			int old = expectedInterceptorPosition.get();
			expectedInterceptorPosition.set(expectedInterceptorPosition.get() + 1);
			return old;
		}

		private final int order;

		public OrderCheckingInterceptor(int order) {
			this.order = order;
		}

		public Object invoke(MethodInvocation mi) throws Throwable {
			assertEquals("Order value matches", order, getPosition());
			return mi.proceed();
		}
	}

	public static class InterceptAllAdvice {
		public static int count;

		@Bean
		@SpringAdvice(matchAll = true)
		protected MethodBeforeAdvice count() {
			return new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) throws Throwable {
					// new Throwable().printStackTrace();
					++count;
				}
			};
		}

		@Bean
		public TestBean kaare() {
			return new TestBean();
		}
	}

	// FIXME: interception on self causes circular dependency - can this be
	// prevented?
	public void tstInterceptAll() throws Exception {
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