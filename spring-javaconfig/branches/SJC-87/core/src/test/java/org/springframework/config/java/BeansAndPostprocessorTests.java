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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.dao.support.PersistenceExceptionTranslationInterceptor;
import org.springframework.stereotype.Repository;

/**
 * @author Costin Leau
 * @author Chris Beams
 */
public class BeansAndPostprocessorTests {

	private ConfigurableJavaConfigApplicationContext ctx;
	private CountingBPP bpp;


	@Before
	public void setUp() {
		bpp = new CountingBPP();
		ctx = new LegacyJavaConfigApplicationContext(MixedConfig.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.addBeanPostProcessor(bpp);
			}
		};
	}
	public static class MixedConfig {
		public @Bean Object publicBean() { return "public bean"; }
		protected @Bean Object hiddenBean() { return "hidden bean"; }
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


	@After
	public void tearDown() { ctx.close(); }


	/**
	 * TODO: this test fails with an error stating that no persistence exception
	 * translation post processors could be found in the bean factory, even
	 * though one is explicitly declared in NewConfiguration below. Remove the
	 * Ignore annotation and diagnose.
	 */
	@Ignore
	@Test
	public void testViaApplicationContext() {
		LegacyJavaConfigApplicationContext context = new LegacyJavaConfigApplicationContext(NewConfiguration.class);
		String name = "newBean";
		context.getBean(Object.class, name);
		CountingBPP postProcessor = context.getBean(CountingBPP.class);
		assertTrue(postProcessor.beans.containsKey(name));
	}
	public static class NewConfiguration {
		public @Bean Object newBean() { return "new bean"; }
		public @Bean FooRepository repos() { return new MyRepository(); }
		public @Bean PersistenceExceptionTranslationInterceptor intercept() {
			return new PersistenceExceptionTranslationInterceptor();
		}
		public @Bean PersistenceExceptionTranslationPostProcessor beanPostProcessor() {
			return new PersistenceExceptionTranslationPostProcessor();
		}
	}
	public static interface FooRepository { }
	public static @Repository class MyRepository implements FooRepository { }


	public @Test void testBPPForPublicBeans() throws Exception {
		String name = "publicBean";
		Object bean = ctx.getBean(name);
		assertNotNull(bean);
		assertTrue(bpp.beans.containsKey(name));
	}


	// TODO: [hiding]
	public @Test void testBPPForHiddenBeans() throws Exception {
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
