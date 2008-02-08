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
package org.springframework.config.java.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.easymock.EasyMock;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;

public class ExternalBeanMethodProcessorTests {
	private ExternalBeanMethodProcessor externalBeanMethodProcessor;

	private ConfigurableListableBeanFactory owningBeanFactory;

	@Before
	public void setUp() {
		owningBeanFactory = EasyMock.createMock(ConfigurableListableBeanFactory.class);

		BeanNamingStrategy namingStrategy = new MethodNameStrategy();
		externalBeanMethodProcessor = new ExternalBeanMethodProcessor(owningBeanFactory, namingStrategy);
	}

	@Test
	public void test() throws SecurityException, NoSuchMethodException {
		EasyMock.expect(owningBeanFactory.getBean("bar")).andReturn(new TestBean("bar"));
		EasyMock.replay(owningBeanFactory);

		Method barMethod = MyConfig.class.getDeclaredMethod("bar");
		TestBean barBean = (TestBean) externalBeanMethodProcessor.processMethod(barMethod);
		Assert.assertThat(barBean.getName(), equalTo("bar"));
		EasyMock.verify(owningBeanFactory);
	}

	@Test
	public void testOverridenBeanName() throws SecurityException, NoSuchMethodException {
		EasyMock.expect(owningBeanFactory.getBean("overriddenName")).andReturn(new TestBean("bar"));
		EasyMock.replay(owningBeanFactory);

		Method barMethod = MyConfigWithOverride.class.getDeclaredMethod("bar");
		TestBean barBean = (TestBean) externalBeanMethodProcessor.processMethod(barMethod);
		assertThat(barBean.getName(), equalTo("bar"));
		EasyMock.verify(owningBeanFactory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testProcessNonAnnotatedMethod() throws SecurityException, NoSuchMethodException {
		Method unadornedMethod = MyConfig.class.getDeclaredMethod("unadorned");
		externalBeanMethodProcessor.processMethod(unadornedMethod);
	}

	@Test
	public void testUnderstands() throws SecurityException, NoSuchMethodException {
		Method barMethod = MyConfig.class.getDeclaredMethod("bar");
		assertTrue(ExternalBeanMethodProcessor.isExternalBeanCreationMethod(barMethod));
	}

	@Test
	public void testNonCandidate() throws SecurityException, NoSuchMethodException {
		Method fooMethod = MyConfig.class.getDeclaredMethod("foo");
		assertFalse("expected non-annotated method to fail isCandidate() check", ExternalBeanMethodProcessor
				.isExternalBeanCreationMethod(fooMethod));
	}

	@Test
	public void testSubClassInheritsAnnotation() throws SecurityException, NoSuchMethodException {
		EasyMock.expect(owningBeanFactory.getBean("bar")).andReturn(new TestBean("bar"));
		EasyMock.replay(owningBeanFactory);

		Method barMethod = SubConfig.class.getDeclaredMethod("bar");
		TestBean barBean = (TestBean) externalBeanMethodProcessor.processMethod(barMethod);
		assertThat(barBean.getName(), equalTo("bar"));
		EasyMock.verify(owningBeanFactory);
	}

	@Test
	public void testGetExternalBeanCreationMethods_1() throws SecurityException, NoSuchMethodException {
		Collection<Method> annotatedMethods = ExternalBeanMethodProcessor
				.findExternalBeanCreationMethods(MyConfig.class);

		assertTrue(annotatedMethods.contains(MyConfig.class.getDeclaredMethod("bar")));
		assertThat(annotatedMethods.size(), CoreMatchers.equalTo(1));
	}

	@Test
	public void testGetExternalBeanCreationMethods_2() throws SecurityException, NoSuchMethodException {
		Collection<Method> annotatedMethods = ExternalBeanMethodProcessor
				.findExternalBeanCreationMethods(SubConfig.class);

		assertTrue(annotatedMethods.contains(SubConfig.class.getDeclaredMethod("bar")));
		assertTrue(annotatedMethods.contains(SubConfig.class.getDeclaredMethod("baz")));
		assertThat(annotatedMethods.size(), CoreMatchers.equalTo(2));
	}

	@Test
	public void testPrivateMethodsExcluded() {
		Collection<Method> annotatedMethods = ExternalBeanMethodProcessor
				.findExternalBeanCreationMethods(InvalidConfigWithPrivateBean.class);

		assertThat(annotatedMethods.size(), CoreMatchers.equalTo(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrivateMethodRejected() throws SecurityException, NoSuchMethodException {
		Method privateMethod = InvalidConfigWithPrivateBean.class.getDeclaredMethod("invalid");
		externalBeanMethodProcessor.processMethod(privateMethod);
	}

	@Configuration
	abstract static class MyConfig {
		@Bean
		TestBean foo() {
			return new TestBean("foo");
		}

		@ExternalBean
		abstract TestBean bar();

		Object unadorned() {
			return new Object();
		}
	}

	@Configuration
	abstract static class MyConfigWithOverride {
		@Bean
		TestBean foo() {
			return new TestBean("foo");
		}

		@ExternalBean("overriddenName")
		abstract TestBean bar();
	}

	static class SubConfig extends MyConfig {
		// should be treated as annotated with @ExternalBean via superclass
		@Override
		TestBean bar() {
			return new TestBean("bar");
		}

		@ExternalBean
		TestBean baz() {
			return new TestBean("baz");
		}

		Object localUnadorned() {
			return new Object();
		}
	}

	static class InvalidConfigWithPrivateBean {
		@SuppressWarnings("unused")
		@ExternalBean
		private Object invalid() {
			return null;
		}
	}
}
