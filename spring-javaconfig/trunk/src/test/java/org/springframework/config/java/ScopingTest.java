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

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.listener.registry.ConfigurationListenerRegistry;
import org.springframework.config.java.listener.registry.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.template.ConfigurationSupport;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test that scopes are properly supported by using a custom scope.
 * 
 * @author Costin Leau
 * 
 */
public class ScopingTest extends TestCase {

	private static final String SCOPE = "my scope";

	private static String flag = "1";

	private static boolean[] createNewScope = new boolean[] { true };

	public static class ScopedConfigurationWithClass extends ConfigurationSupport {

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
	}

	/**
	 * Simple scope implementation which creates object based on a flag.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private static class MyCustomScope implements Scope {

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

	private ConfigurationListenerRegistry clr;

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	private ConfigurableApplicationContext appCtx;

	private Scope scope;

	@Override
	protected void setUp() throws Exception {
		clr = new DefaultConfigurationListenerRegistry();
		bf = new DefaultListableBeanFactory();
		configurationProcessor = new ConfigurationProcessor(bf, clr);
		scope = new MyCustomScope();
		// register the scope
		bf.registerScope(SCOPE, scope);

		// and do the processing
		configurationProcessor.processClass(ScopedConfigurationWithClass.class);
	}

	@Override
	protected void tearDown() throws Exception {
		bf.destroySingletons();
		bf = null;
		clr = null;
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

}
