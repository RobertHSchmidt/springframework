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

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 
 * @author Rod Johnson
 * 
 */
public class HiddenBeanPostProcessorTests {

	@Configuration
	public static class HasHiddenBeansNeedingPostProcessing {

		@Bean
		public Point publicPoint() {
			return protectedPoint();
		}

		@Bean
		protected Point protectedPoint() {
			return packagePoint();
		}

		@Bean
		Point packagePoint() {
			return new Point();
		}

	}

	public static class RememberingBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

		private Map<String, Class<?>> beansSeen = new HashMap<String, Class<?>>();

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			// System.out.println(beanName + "=" + bean.getClass());
			if (beansSeen.containsKey(beanName)) {
				throw new IllegalStateException("Cannot process a bean twice");
			}
			beansSeen.put(beanName, bean.getClass());
			return bean;
		}
	}

	@Test
	public void testBeanPostProcessorActsOnHiddenBeans() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		RememberingBeanPostProcessor rememberingPostProcessor = new RememberingBeanPostProcessor();
		bf.addBeanPostProcessor(rememberingPostProcessor);
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);

		configurationProcessor.processClass(HasHiddenBeansNeedingPostProcessing.class);

		// Will get others
		bf.getBean("publicPoint");

		Assert.assertTrue("Must have recorded public bean method", rememberingPostProcessor.beansSeen
				.containsKey("publicPoint"));
		Assert.assertTrue("Must have recorded package bean method", rememberingPostProcessor.beansSeen
				.containsKey("packagePoint"));
		Assert.assertTrue("Must have recorded protected bean method", rememberingPostProcessor.beansSeen
				.containsKey("protectedPoint"));
	}

	public static class RememberingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

		private Set<String> beansSeen = new HashSet<String>();

		public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
			for (String beanName : clbf.getBeanDefinitionNames()) {

				// System.out.println(beanName + "=" + bean.getClass());
				if (beansSeen.contains(beanName)) {
					throw new IllegalStateException("Cannot process a bean twice");
				}
				beansSeen.add(beanName);
			}
		}
	}

	// TODO decision on application context/BFPP running in child context
	@Ignore
	@Test
	public void testBeanFactoryPostProcessorActsOnHiddenBeans() {
		// BeanFactoryPostProcessors work only on ApplicationContext
		GenericApplicationContext gac = new GenericApplicationContext();

		RememberingBeanFactoryPostProcessor rememberingFactoryPostProcessor = new RememberingBeanFactoryPostProcessor();
		gac.addBeanFactoryPostProcessor(rememberingFactoryPostProcessor);
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(gac);
		configurationProcessor.processClass(HasHiddenBeansNeedingPostProcessing.class);

		gac.refresh();

		Assert.assertTrue("Must have recorded public bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("publicPoint"));
		Assert.assertTrue("Must have recorded package bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("packagePoint"));
		Assert.assertTrue("Must have recorded protected bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("protectedPoint"));
	}

	@Ignore
	@Test
	public void testApplicationContextWithXml() {
		ClassPathXmlApplicationContext cac = new ClassPathXmlApplicationContext(
				new String[] { "/org/springframework/config/java/hiddenBeanPostProcessorTests.xml" }, true);
		// cac.refresh();

		RememberingBeanFactoryPostProcessor rememberingFactoryPostProcessor = (RememberingBeanFactoryPostProcessor) cac
				.getBean("bfpp");

		Assert.assertTrue("Must have recorded public bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("publicPoint"));
		Assert.assertTrue("Must have recorded package bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("packagePoint"));
		Assert.assertTrue("Must have recorded protected bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("protectedPoint"));
	}

	@Configuration
	static class ScopedContext {
		@Bean(scope = "fooScope")
		public Object foo() {
			return new Object();
		}
	}

	// Note: This class would not work as a scope
	// We are only trying to check Scope registration, not functionality
	public class DummyScope implements Scope {

		public Object get(String s, ObjectFactory of) {
			return null;
		}

		public String getConversationId() {
			return null;
		}

		public void registerDestructionCallback(String name, Runnable r) {
		}

		public Object remove(String name) {
			return null;
		}
	}

	@Test
	public void testScopesCopiedFromParent() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerScope("fooScope", new DummyScope());
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(bf);

		configurationProcessor.processClass(ScopedContext.class);
		bf.getBean("foo");
	}
}