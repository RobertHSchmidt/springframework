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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
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

	private static boolean[] createNewScope = new boolean[] { true };

	public static final String SCOPE = "my scope";

	public static String flag = "1";

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

	/**
	 * Simple scope implementation which creates object based on a flag.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static class MyCustomScope implements Scope {

		private Map<String, Object> beans = new HashMap<String, Object>();

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
		 * org.springframework.beans.factory.ObjectFactory)
		 */
		public Object get(String name, ObjectFactory objectFactory) {

			if (createNewScope[0]) {
				beans.clear();
				// reset the flag back
				createNewScope[0] = false;
			}

			Object bean = beans.get(name);
			// if a new object is requested or none exists under the current
			// name, create one
			if (bean == null) {
				beans.put(name, objectFactory.getObject());
			}
			return beans.get(name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#getConversationId()
		 */
		public String getConversationId() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
		 * java.lang.Runnable)
		 */
		public void registerDestructionCallback(String name, Runnable callback) {
			// do nothing
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
		 */
		public Object remove(String name) {
			return beans.remove(name);
		}

	}

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	private ConfigurableApplicationContext appCtx;

	private Scope scope;

	@Override
	protected void setUp() throws Exception {
		bf = new DefaultListableBeanFactory();
		configurationProcessor = new ConfigurationProcessor(bf);
		scope = new MyCustomScope();
		// register the scope
		bf.registerScope(SCOPE, scope);

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

		scope = null;
	}

	public void genericTestScope(String beanName) throws Exception {

		String message = "scope is ignored";
		Object bean1 = bf.getBean(beanName);
		Object bean2 = bf.getBean(beanName);

		assertSame(message, bean1, bean2);

		Object bean3 = bf.getBean(beanName);

		assertSame(message, bean1, bean3);

		// make the scope create a new object
		createNewScope[0] = true;

		Object newBean1 = bf.getBean(beanName);
		assertNotSame(message, bean1, newBean1);

		Object sameBean1 = bf.getBean(beanName);

		assertSame(message, newBean1, sameBean1);

		// make the scope create a new object
		createNewScope[0] = true;

		Object newBean2 = bf.getBean(beanName);
		assertNotSame(message, newBean1, newBean2);

		// make the scope create a new object .. again
		createNewScope[0] = true;

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

		createNewScope[0] = true;

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
		createNewScope[0] = true;

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
		createNewScope[0] = true;
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
		anotherBf.registerScope(SCOPE, scope);

		ConfigurationProcessor processor = new ConfigurationProcessor(anotherBf);
		// count the beans
		// 6 @Beans + 1 Configuration + 2 @ScopedProxy
		assertEquals(9, processor.processClass(ScopedConfigurationClass.class));
	}

}
