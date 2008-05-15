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
package org.springframework.config.java.support;

import static org.junit.Assert.*;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Rod Johnson
 * @author Chris Beams
 */
public class ConfigurationSupportTests {

	private static class TestFactoryBeanForBeanFactory implements FactoryBean, InitializingBean, BeanFactoryAware,
			BeanClassLoaderAware {

		private final Object expectedReturnedObject;

		private BeanFactory bf;

		private ClassLoader beanClassLoader;

		private boolean afterPropertiesSetCalled;

		public TestFactoryBeanForBeanFactory(Object expObject) {
			this.expectedReturnedObject = expObject;
		}

		public void afterPropertiesSet() throws Exception {
			afterPropertiesSetCalled = true;
		}

		public void setBeanClassLoader(ClassLoader cl) {
			this.beanClassLoader = cl;
		}

		public void setBeanFactory(BeanFactory bf) throws BeansException {
			this.bf = bf;
		}

		public Object getObject() throws Exception {
			assertTrue(afterPropertiesSetCalled);
			assertNotNull("Must have beanFactory set", bf);
			assertNotNull("Must have beanClassLoader set", beanClassLoader);

			return expectedReturnedObject;
		}

		public Class<?> getObjectType() {
			return null;
		}

		public boolean isSingleton() {
			return true;
		}
	}

	private static class TestFactoryBeanForAppContext extends TestFactoryBeanForBeanFactory implements
			ResourceLoaderAware, ApplicationContextAware {
		private ResourceLoader rl;

		private ApplicationContext ac;

		public TestFactoryBeanForAppContext(Object o) {
			super(o);
		}

		public void setResourceLoader(ResourceLoader rl) {
			this.rl = rl;
		}

		public void setApplicationContext(ApplicationContext ac) throws BeansException {
			this.ac = ac;
		}

		@Override
		public Object getObject() throws Exception {
			assertNotNull("Must have ResourceLoader set", rl);
			assertNotNull("Must have ApplicationContext set", ac);

			return super.getObject();
		}

	}

	@Configuration
	public static class FactoryBeanTest extends ConfigurationSupport {
		@Bean
		public Object factoryTestBean() {
			return getObject(new TestFactoryBeanForBeanFactory("whatever"));
		}
	}

	@Configuration
	public static class ApplicationContextTest extends ConfigurationSupport {
		@Bean
		public Object factoryTestBean() {
			return getObject(new TestFactoryBeanForAppContext("whatever"));
		}
	}

	@Configuration
	public static class Config1 extends ConfigurationSupport {
		@Bean
		public TestBean foo() {
			return new TestBean((String) getBean("name"));
		}
	}

	@Configuration
	public static class Config2 {
		@Bean
		public String name() {
			return "xyz";
		}
	}

	@Configuration
	public static class Config3 extends ConfigurationSupport {
		@Bean
		public TestBean foo() {
			// return new TestBean(getBean(String.class));
			return new TestBean((String) getBean("name"));
		}
	}

	@Configuration
	public static class TypeSafeConfig extends ConfigurationSupport {
		@Bean
		public TestBean foo() {
			return new TestBean(getBean(String.class));
		}
	}

	@Configuration
	public static class TypeSafeConfigWithStringQualifier extends ConfigurationSupport {
		@Bean
		public TestBean foo() {
			return new TestBean(getBean(String.class, "name"));
		}
	}

	@Test
	public void testFactoryBeanCallbacks() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(FactoryBeanTest.class);
		assertEquals("whatever", ctx.getBean("factoryTestBean"));
	}

	@Test
	public void testApplicationContextCallbacks() {
		GenericApplicationContext gac = new GenericApplicationContext();
		ConfigurationPostProcessor cpp = new ConfigurationPostProcessor();
		cpp.setApplicationContext(gac);
		gac.getDefaultListableBeanFactory().registerSingleton("not_significant", cpp);

		gac.getDefaultListableBeanFactory().registerBeanDefinition("doesnt_matter",
				new RootBeanDefinition(ApplicationContextTest.class));

		gac.refresh();
		assertEquals("whatever", gac.getBean("factoryTestBean"));
	}

	@Test
	public void testStringBasedGetBean() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(Config1.class, Config2.class);
		assertThat("xyz", CoreMatchers.equalTo(((TestBean) ctx.getBean("foo")).getName()));
	}

	@Test
	public void testTypeSafeGetBean() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(TypeSafeConfig.class, Config2.class);
		assertThat("xyz", CoreMatchers.equalTo(((TestBean) ctx.getBean("foo")).getName()));
	}

	@Test
	public void testTypeSafeGetBeanWithStringQualifier() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(TypeSafeConfigWithStringQualifier.class,
				Config2.class);
		assertThat("xyz", CoreMatchers.equalTo(((TestBean) ctx.getBean("foo")).getName()));
	}
}
