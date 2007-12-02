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
package org.springframework.config.java;

import junit.framework.TestCase;

import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.config.java.util.ScopeUtils;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test that scopes are properly supported by using a custom scope and scoped
 * proxies.
 * 
 * @author Costin Leau
 */
public class ScopingTests extends TestCase {

	public static final String SCOPE = "my scope";

	public static String flag = "1";

	private CustomScope customScope = new CustomScope();

	public static class ScopedConfigurationClass extends ConfigurationSupport {

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

		@Bean(scope = SCOPE)
		@ScopedProxy
		public TestBean scopedProxyClass() {
			TestBean tb = new TestBean();
			tb.setName(flag);
			return tb;
		}

		@Bean
		public TestBean singletonWithScopedClassDep() {
			TestBean singleton = new TestBean();
			singleton.setSpouse(scopedProxyClass());
			return singleton;
		}

		@Bean
		public TestBean singletonWithScopedInterfaceDep() {
			TestBean singleton = new TestBean();
			singleton.setSpouse(scopedProxyInterface());
			return singleton;
		}

	}

	@Configuration
	public static class InvalidProxyObjectConfiguration {

		@ScopedProxy
		public Object invalidProxyObject() {
			return new Object();
		}

	}

	@Configuration
	public static class InvalidProxyOnPredefinedScopesConfiguration {

		@Bean
		@ScopedProxy
		public Object invalidProxyOnPredefinedScopes() {
			return new Object();
		}

	}

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	private ConfigurableApplicationContext appCtx;

	@Override
	protected void setUp() throws Exception {
		bf = new DefaultListableBeanFactory();
		configurationProcessor = new ConfigurationProcessor(bf);
		customScope = new CustomScope();
		// register the scope
		bf.registerScope(SCOPE, customScope);

		// and do the processing
		configurationProcessor.processClass(ScopedConfigurationClass.class);
	}

	@Override
	protected void tearDown() throws Exception {
		bf.destroySingletons();
		bf = null;
		configurationProcessor = null;
		if (appCtx != null && appCtx.isActive())
			appCtx.close();

		customScope = null;
	}

	public void genericTestScope(String beanName) throws Exception {

		String message = "scope is ignored";
		Object bean1 = bf.getBean(beanName);
		Object bean2 = bf.getBean(beanName);

		assertSame(message, bean1, bean2);

		Object bean3 = bf.getBean(beanName);

		assertSame(message, bean1, bean3);

		// make the scope create a new object
		customScope.createNewScope = true;

		Object newBean1 = bf.getBean(beanName);
		assertNotSame(message, bean1, newBean1);

		Object sameBean1 = bf.getBean(beanName);

		assertSame(message, newBean1, sameBean1);

		// make the scope create a new object
		customScope.createNewScope = true;

		Object newBean2 = bf.getBean(beanName);
		assertNotSame(message, newBean1, newBean2);

		// make the scope create a new object .. again
		customScope.createNewScope = true;

		Object newBean3 = bf.getBean(beanName);
		assertNotSame(message, newBean2, newBean3);
	}

	public void testScopeOnClasses() throws Exception {
		genericTestScope("scopedClass");
	}

	public void testScopeOnInterfaces() throws Exception {
		genericTestScope("scopedInterface");
	}

	public void testSameScopeOnDifferentBeans() throws Exception {
		Object beanAInScope = bf.getBean("scopedClass");
		Object beanBInScope = bf.getBean("scopedInterface");

		assertNotSame(beanAInScope, beanBInScope);

		customScope.createNewScope = true;

		Object newBeanAInScope = bf.getBean("scopedClass");
		Object newBeanBInScope = bf.getBean("scopedInterface");

		assertNotSame(newBeanAInScope, newBeanBInScope);
		assertNotSame(newBeanAInScope, beanAInScope);
		assertNotSame(newBeanBInScope, beanBInScope);
	}

	public void testInvalidScopedProxy() throws Exception {
		try {
			configurationProcessor.processClass(InvalidProxyObjectConfiguration.class);
			fail("@ScopedProxy should not exist by itself");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testScopedProxyOnNonBeanAnnotatedMethod() throws Exception {
		try {
			configurationProcessor.processClass(InvalidProxyOnPredefinedScopesConfiguration.class);
			fail("@ScopedProxy should not be applied on singleton/prototype beans");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testRawScopes() throws Exception {
		String beanName = "scopedProxyInterface";
		// get hidden bean
		Object bean = bf.getBean("scopedTarget." + beanName);

		assertFalse(bean instanceof ScopedObject);
	}

	public void testScopedProxyConfiguration() throws Exception {

		TestBean singleton = (TestBean) bf.getBean("singletonWithScopedInterfaceDep");
		ITestBean spouse = singleton.getSpouse();
		assertTrue("scoped bean is not wrapped by the scoped-proxy", spouse instanceof ScopedObject);

		String beanName = "scopedProxyInterface";

		String scopedBeanName = ScopeUtils.getScopedHiddenName(beanName);

		// get hidden bean
		assertEquals(flag, spouse.getName());

		ITestBean spouseFromBF = (ITestBean) bf.getBean(scopedBeanName);
		assertEquals(spouse.getName(), spouseFromBF.getName());
		// the scope proxy has kicked in
		assertNotSame(spouse, spouseFromBF);

		// create a new bean
		customScope.createNewScope = true;

		// get the bean again from the BF
		spouseFromBF = (ITestBean) bf.getBean(scopedBeanName);
		// make sure the name has been updated
		assertSame(spouse.getName(), spouseFromBF.getName());
		assertNotSame(spouse, spouseFromBF);

		// get the bean again
		spouseFromBF = (ITestBean) bf.getBean(scopedBeanName);
		assertSame(spouse.getName(), spouseFromBF.getName());
	}

	public void testScopedProxyConfigurationWithClasses() throws Exception {

		TestBean singleton = (TestBean) bf.getBean("singletonWithScopedClassDep");
		ITestBean spouse = singleton.getSpouse();
		assertTrue("scoped bean is not wrapped by the scoped-proxy", spouse instanceof ScopedObject);

		String beanName = "scopedProxyClass";

		String scopedBeanName = ScopeUtils.getScopedHiddenName(beanName);

		// get hidden bean
		assertEquals(flag, spouse.getName());

		TestBean spouseFromBF = (TestBean) bf.getBean(scopedBeanName);
		assertEquals(spouse.getName(), spouseFromBF.getName());
		// the scope proxy has kicked in
		assertNotSame(spouse, spouseFromBF);

		// create a new bean
		customScope.createNewScope = true;
		flag = "boo";

		// get the bean again from the BF
		spouseFromBF = (TestBean) bf.getBean(scopedBeanName);
		// make sure the name has been updated
		assertSame(spouse.getName(), spouseFromBF.getName());
		assertNotSame(spouse, spouseFromBF);

		// get the bean again
		spouseFromBF = (TestBean) bf.getBean(scopedBeanName);
		assertSame(spouse.getName(), spouseFromBF.getName());
	}

	public void testScopedConfigurationBeanDefinitionCount() throws Exception {
		DefaultListableBeanFactory anotherBf = new DefaultListableBeanFactory();
		// register the scope
		anotherBf.registerScope(SCOPE, customScope);

		ConfigurationProcessor processor = new ConfigurationProcessor(anotherBf);
		// count the beans
		// 6 @Beans + 1 Configuration + 2 @ScopedProxy
		assertEquals(9, processor.processClass(ScopedConfigurationClass.class));
	}

}
