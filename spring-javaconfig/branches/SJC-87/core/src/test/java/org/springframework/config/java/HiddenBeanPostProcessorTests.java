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

import static junit.framework.Assert.assertTrue;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rod Johnson
 * @author Chris Beams
 */
public class HiddenBeanPostProcessorTests {

	public @Test void testBeanPostProcessorActsOnHiddenBeans() {
		final RememberingBeanPostProcessor rememberingPostProcessor = new RememberingBeanPostProcessor();

		new JavaConfigApplicationContext(HasHiddenBeansNeedingPostProcessing.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.addBeanPostProcessor(rememberingPostProcessor);
			}
		}.getBean("publicPoint");

		assertTrue("Must have recorded public bean method",
				rememberingPostProcessor.beansSeen.containsKey("publicPoint"));
		assertTrue("Must have recorded package bean method",
				rememberingPostProcessor.beansSeen.containsKey("packagePoint"));
		assertTrue("Must have recorded protected bean method",
				rememberingPostProcessor.beansSeen.containsKey("protectedPoint"));
	}
	public static class RememberingBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
		private Map<String, Class<?>> beansSeen = new HashMap<String, Class<?>>();

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (beansSeen.containsKey(beanName)) {
				throw new IllegalStateException("Cannot process a bean twice");
			}
			beansSeen.put(beanName, bean.getClass());
			return bean;
		}
	}


	public @Test void testBeanFactoryPostProcessorActsOnHiddenBeans() {
		RememberingBeanFactoryPostProcessor rememberingFactoryPostProcessor = new RememberingBeanFactoryPostProcessor();

		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.addBeanFactoryPostProcessor(rememberingFactoryPostProcessor);
		ctx.addConfigClasses(HasHiddenBeansNeedingPostProcessing.class);
		ctx.refresh();

		validateBeanFactoryPostProcessor(rememberingFactoryPostProcessor);
	}
	public static class HasHiddenBeansNeedingPostProcessing {
		public @Bean Point publicPoint() { return protectedPoint(); }
		protected @Bean Point protectedPoint() { return packagePoint(); }
		@Bean Point packagePoint() { return new Point(); }
	}


	public @Test void testApplicationContextWithXml() {
		ClassPathXmlApplicationContext cac = new ClassPathXmlApplicationContext(
				new String[] { "/org/springframework/config/java/hiddenBeanPostProcessorTests.xml" }, true);

		RememberingBeanFactoryPostProcessor rememberingFactoryPostProcessor =
			(RememberingBeanFactoryPostProcessor) cac.getBean("bfpp1");
		validateBeanFactoryPostProcessor(rememberingFactoryPostProcessor);

		rememberingFactoryPostProcessor = (RememberingBeanFactoryPostProcessor) cac.getBean("bfpp2");
		validateBeanFactoryPostProcessor(rememberingFactoryPostProcessor);
	}


	private static class RememberingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		private Set<String> beansSeen = new HashSet<String>();

		public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
			for (String beanName : clbf.getBeanDefinitionNames()) {

				if (beansSeen.contains(beanName)) {
					throw new IllegalStateException("Cannot process a bean twice");
				}
				beansSeen.add(beanName);
			}
		}
	}


	private void validateBeanFactoryPostProcessor(RememberingBeanFactoryPostProcessor rememberingFactoryPostProcessor) {
		Assert.assertTrue("Must have recorded public bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("publicPoint"));
		Assert.assertTrue("Must have recorded package bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("packagePoint"));
		Assert.assertTrue("Must have recorded protected bean method", rememberingFactoryPostProcessor.beansSeen
				.contains("protectedPoint"));
	}


	// XXX: [bean scoping]
	public @Test void testScopesCopiedFromParent() {
		new JavaConfigApplicationContext(ScopedContextConfig.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.registerScope("fooScope", new DummyScope());
			}
		}.getBean("foo");
	}
	static class ScopedContextConfig {
		@Bean(scope = "fooScope")
		public Object foo() { return new Object(); }
	}
	// Note: This class would not work as a scope
	// We are only trying to check Scope registration, not functionality
	public class DummyScope implements Scope {
		public Object get(String s, ObjectFactory of) { return null; }
		public String getConversationId() { return null; }
		public void registerDestructionCallback(String name, Runnable r) { }
		public Object remove(String name) { return null; }
	}

}
