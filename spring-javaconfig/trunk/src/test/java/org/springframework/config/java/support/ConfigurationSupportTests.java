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

package org.springframework.config.java.support;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;

/**
 *
 * @author Rod Johnson
 *
 */
public class ConfigurationSupportTests extends TestCase {

	private static class TestFactoryBeanForBeanFactory implements FactoryBean, InitializingBean, BeanFactoryAware {

		private final Object expectedReturnedObject;

		private BeanFactory bf;

		private boolean afterPropertiesSetCalled;

		public TestFactoryBeanForBeanFactory(Object expObject) {
			this.expectedReturnedObject = expObject;
		}

		public void afterPropertiesSet() throws Exception {
			afterPropertiesSetCalled = true;
		}

		public void setBeanFactory(BeanFactory bf) throws BeansException {
			this.bf = bf;
		}

		public Object getObject() throws Exception {
			TestCase.assertTrue(afterPropertiesSetCalled);
			TestCase.assertNotNull("Must have beanFactory set", bf);

			return expectedReturnedObject;
		}

		public Class<?> getObjectType() {
			return null;
		}

		public boolean isSingleton() {
			return true;
		}
	}

	private static class TestFactoryBeanForAppContext extends TestFactoryBeanForBeanFactory
													implements ResourceLoaderAware, ApplicationContextAware {
		private ResourceLoader rl;
		private ApplicationContext ac;

		public TestFactoryBeanForAppContext(Object o) {
			super(o);
		}

		public void setResourceLoader(ResourceLoader rl) {
			this.rl = rl;
		}

		public void setApplicationContext(ApplicationContext ac)
				throws BeansException {
			this.ac = ac;
		}

		@Override
		public Object getObject() throws Exception {
			TestCase.assertNotNull("Must have ResourceLoader set", rl);
			TestCase.assertNotNull("Must have ApplicationContext set", ac);

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

	public void testFactoryBeanCallbacks() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);

		configurationProcessor.processClass(FactoryBeanTest.class);
		assertEquals("whatever", bf.getBean("factoryTestBean"));
	}

	public void testApplicationContextCallbacks() {
		GenericApplicationContext gac = new GenericApplicationContext();
		//ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(gac);
		ConfigurationPostProcessor cpp = new ConfigurationPostProcessor();
		gac.getDefaultListableBeanFactory().registerSingleton("not_significant", cpp);

		gac.getDefaultListableBeanFactory().registerBeanDefinition(
				"doesnt_matter", new RootBeanDefinition(ApplicationContextTest.class));

		gac.refresh();
		assertEquals("whatever", gac.getBean("factoryTestBean"));
	}

}
