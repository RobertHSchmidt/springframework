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

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.dao.support.PersistenceExceptionTranslationInterceptor;
import org.springframework.stereotype.Repository;

/**
 * @author Costin Leau
 * 
 */
public class BeansAndPostprocessorTests extends TestCase {

	@Configuration
	public static class MixedConfiguration {
		@Bean
		public Object publicBean() {
			return "public bean";
		}

		@Bean
		protected Object hiddenBean() {
			return "hidden bean";
		}
	}

	public static class CountingBPP implements BeanPostProcessor {

		public Map<String, Object> beans = new LinkedHashMap<String, Object>();

		public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
			beans.put(name, bean);
			return bean;
		}

		public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
			return bean;
		}

	}

	public static class NewConfiguration {
		@Bean
		public Object newBean() {
			return "new bean";
		}

		@Bean
		public FooRepository repos() {
			return new MyRepository();
		}

		@Bean
		public PersistenceExceptionTranslationInterceptor intercept() {
			return new PersistenceExceptionTranslationInterceptor();
		}

		@Bean
		public BeanPostProcessor beanPostProcessor() {
			PersistenceExceptionTranslationPostProcessor bpp = new PersistenceExceptionTranslationPostProcessor();
			return bpp;
		}

	}

	public static interface FooRepository {
	}

	@Repository
	public static class MyRepository implements FooRepository {

	}

	public void testViaApplicationContext() {
		JavaConfigApplicationContext context = new JavaConfigApplicationContext(NewConfiguration.class);
		String name = "newBean";
		Object newBean = context.getBean(Object.class, name);
		CountingBPP postProcessor = context.getBean(CountingBPP.class);
		assertTrue(postProcessor.beans.containsKey(name));
	}

	private ConfigurableApplicationContext ctx;

	private ConfigurationProcessor configurationProcessor;

	private CountingBPP bpp;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		ctx = new GenericApplicationContext();
		configurationProcessor = new ConfigurationProcessor(ctx);
		configurationProcessor.afterPropertiesSet();

		configurationProcessor.processClass(MixedConfiguration.class);

		bpp = new CountingBPP();
		ctx.getBeanFactory().addBeanPostProcessor(bpp);
		ctx.refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		ctx.close();
	}

	public void testBPPForPublicBeans() throws Exception {
		String name = "publicBean";
		Object bean = ctx.getBean(name);
		assertNotNull(bean);
		assertTrue(bpp.beans.containsKey(name));
	}

	public void testBPPForHiddenBeans() throws Exception {
		String name = "hiddenBean";
		try {
			ctx.getBean(name);
			fail("should have thrown exception");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// expected
		}
		// deal with BPP
		assertFalse(bpp.beans.containsKey(name));
	}

}
