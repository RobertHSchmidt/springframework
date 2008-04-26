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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.config.java.core.ScopedProxyMethodProcessor;

/**
 * Test that scopes are properly supported by using a custom scope and scoped
 * proxies.
 *
 * @author Costin Leau
 * @author Chris Beams
 */
public class ScopingTests {

	public static String flag = "1";

	private static final String SCOPE = "my scope";
	private CustomScope customScope;
	private ConfigurableJavaConfigApplicationContext ctx;

	@Before
	public void setUp() throws Exception {
		customScope = new CustomScope();
		// TODO: swap for JCAC (see todos below)
		ctx = new LegacyJavaConfigApplicationContext(ScopedConfigurationClass.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.registerScope(SCOPE, customScope);
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		ctx.close();
		ctx = null;
		customScope = null;
	}


	public @Test void testScopeOnClasses() throws Exception {
		genericTestScope("scopedClass");
	}


	public @Test void testScopeOnInterfaces() throws Exception {
		genericTestScope("scopedInterface");
	}


	public @Test void testSameScopeOnDifferentBeans() throws Exception {
		Object beanAInScope = ctx.getBean("scopedClass");
		Object beanBInScope = ctx.getBean("scopedInterface");

		assertNotSame(beanAInScope, beanBInScope);

		customScope.createNewScope = true;

		Object newBeanAInScope = ctx.getBean("scopedClass");
		Object newBeanBInScope = ctx.getBean("scopedInterface");

		assertNotSame(newBeanAInScope, newBeanBInScope);
		assertNotSame(newBeanAInScope, beanAInScope);
		assertNotSame(newBeanBInScope, beanBInScope);
	}


	// TODO: [@ScopedProxy]
	@Test(expected = BeanDefinitionStoreException.class)
	public void testInvalidScopedProxy() throws Exception {
		// should throw - @ScopedProxy should not exist by itself
		new LegacyJavaConfigApplicationContext(InvalidProxyObjectConfiguration.class);
	}
	@Configuration
	public static class InvalidProxyObjectConfiguration {
		public @ScopedProxy Object invalidProxyObject() { return new Object(); }
	}


	// TODO: [@ScopedProxy]
	@Test(expected = BeanDefinitionStoreException.class)
	public void testScopedProxyOnNonBeanAnnotatedMethod() throws Exception {
		// should throw - @ScopedProxy should not be applied on
		// singleton/prototype beans
		new LegacyJavaConfigApplicationContext(InvalidProxyOnPredefinedScopesConfiguration.class);
	}
	@Configuration
	public static class InvalidProxyOnPredefinedScopesConfiguration {
		public @ScopedProxy @Bean Object invalidProxyOnPredefinedScopes() { return new Object(); }
	}


	public @Test void testRawScopes() throws Exception {
		String beanName = "scopedProxyInterface";
		// get hidden bean
		Object bean = ctx.getBean("scopedTarget." + beanName);

		assertFalse(bean instanceof ScopedObject);
	}


	public @Test void testScopedProxyConfiguration() throws Exception {
		TestBean singleton = (TestBean) ctx.getBean("singletonWithScopedInterfaceDep");
		ITestBean spouse = singleton.getSpouse();
		assertTrue("scoped bean is not wrapped by the scoped-proxy", spouse instanceof ScopedObject);

		String beanName = "scopedProxyInterface";

		String scopedBeanName = ScopedProxyMethodProcessor.resolveHiddenScopedProxyBeanName(beanName);

		// get hidden bean
		assertEquals(flag, spouse.getName());

		ITestBean spouseFromBF = (ITestBean) ctx.getBean(scopedBeanName);
		assertEquals(spouse.getName(), spouseFromBF.getName());
		// the scope proxy has kicked in
		assertNotSame(spouse, spouseFromBF);

		// create a new bean
		customScope.createNewScope = true;

		// get the bean again from the BF
		spouseFromBF = (ITestBean) ctx.getBean(scopedBeanName);
		// make sure the name has been updated
		assertSame(spouse.getName(), spouseFromBF.getName());
		assertNotSame(spouse, spouseFromBF);

		// get the bean again
		spouseFromBF = (ITestBean) ctx.getBean(scopedBeanName);
		assertSame(spouse.getName(), spouseFromBF.getName());
	}


	public @Test void testScopedProxyConfigurationWithClasses() throws Exception {
		TestBean singleton = (TestBean) ctx.getBean("singletonWithScopedClassDep");
		ITestBean spouse = singleton.getSpouse();
		assertTrue("scoped bean is not wrapped by the scoped-proxy", spouse instanceof ScopedObject);

		String beanName = "scopedProxyClass";

		String scopedBeanName = ScopedProxyMethodProcessor.resolveHiddenScopedProxyBeanName(beanName);

		// get hidden bean
		assertEquals(flag, spouse.getName());

		TestBean spouseFromBF = (TestBean) ctx.getBean(scopedBeanName);
		assertEquals(spouse.getName(), spouseFromBF.getName());
		// the scope proxy has kicked in
		assertNotSame(spouse, spouseFromBF);

		// create a new bean
		customScope.createNewScope = true;
		flag = "boo";

		// get the bean again from the BF
		spouseFromBF = (TestBean) ctx.getBean(scopedBeanName);
		// make sure the name has been updated
		assertSame(spouse.getName(), spouseFromBF.getName());
		assertNotSame(spouse, spouseFromBF);

		// get the bean again
		spouseFromBF = (TestBean) ctx.getBean(scopedBeanName);
		assertSame(spouse.getName(), spouseFromBF.getName());
	}


	// TODO: [@ScopedProxy]
	public @Test void testScopedConfigurationBeanDefinitionCount() throws Exception {
		ctx = new LegacyJavaConfigApplicationContext(ScopedConfigurationClass.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.registerScope(SCOPE, customScope);
			}
		};

		// count the beans
		// 6 @Beans + 1 Configuration + 2 @ScopedProxy
		assertEquals(9, ctx.getBeanDefinitionCount());
	}
	public static class ScopedConfigurationClass {
		@Bean(scope = SCOPE)
		public TestBean scopedClass() {
			TestBean tb = new TestBean();
			tb.setName(flag);
			return tb;
		}

		@Bean(scope = SCOPE)
		public ITestBean scopedInterface() {
			TestBean tb = new TestBean();
			tb.setName(flag);
			return tb;
		}

		@Bean(scope = SCOPE)
		@ScopedProxy(proxyTargetClass = false)
		public ITestBean scopedProxyInterface() {
			TestBean tb = new TestBean();
			tb.setName(flag);
			return tb;
		}

		@ScopedProxy @Bean(scope = SCOPE)
		public TestBean scopedProxyClass() {
			TestBean tb = new TestBean();
			tb.setName(flag);
			return tb;
		}

		public @Bean TestBean singletonWithScopedClassDep() {
			TestBean singleton = new TestBean();
			singleton.setSpouse(scopedProxyClass());
			return singleton;
		}

		public @Bean TestBean singletonWithScopedInterfaceDep() {
			TestBean singleton = new TestBean();
			singleton.setSpouse(scopedProxyInterface());
			return singleton;
		}
	}


	private void genericTestScope(String beanName) throws Exception {
		String message = "scope is ignored";
		Object bean1 = ctx.getBean(beanName);
		Object bean2 = ctx.getBean(beanName);

		assertSame(message, bean1, bean2);

		Object bean3 = ctx.getBean(beanName);

		assertSame(message, bean1, bean3);

		// make the scope create a new object
		customScope.createNewScope = true;

		Object newBean1 = ctx.getBean(beanName);
		assertNotSame(message, bean1, newBean1);

		Object sameBean1 = ctx.getBean(beanName);

		assertSame(message, newBean1, sameBean1);

		// make the scope create a new object
		customScope.createNewScope = true;

		Object newBean2 = ctx.getBean(beanName);
		assertNotSame(message, newBean1, newBean2);

		// make the scope create a new object .. again
		customScope.createNewScope = true;

		Object newBean3 = ctx.getBean(beanName);
		assertNotSame(message, newBean2, newBean3);
	}

}
