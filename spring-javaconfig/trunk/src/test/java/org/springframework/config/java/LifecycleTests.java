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

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Bean lifecycle (aware/init/destroy) test.
 * 
 * @author Costin Leau
 * 
 */
public class LifecycleTests extends TestCase {

	private ConfigurationProcessor configurationProcessor;

	private ConfigurableApplicationContext appCtx;

	private static class AwareBean implements InitializingBean, DisposableBean, BeanFactoryAware, BeanNameAware,
			ApplicationContextAware {

		public static int DESTROYED = 0;

		public static int INITIALIZED = 0;

		public static int CUSTOM_DESTROYED = 0;

		public static int CUSTOM_INITIALIZED = 0;

		private BeanFactory factory = null;

		public String name;

		public ApplicationContext appCtx;

		public BeanFactory getBeanFactory() {
			return factory;
		}

		public void destroy() throws Exception {
			DESTROYED++;
		}

		public void afterPropertiesSet() throws Exception {
			INITIALIZED++;
		}

		public void close() {
			CUSTOM_DESTROYED++;
		}

		public void init() {
			CUSTOM_INITIALIZED++;
		}

		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.factory = beanFactory;
		}

		public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
			this.appCtx = appCtx;
		}

		public void setBeanName(String name) {
			this.name = name;
		}

	}

	@Configuration
	public static class Config {
		@Bean(lazy = Lazy.TRUE)
		public AwareBean simple() {
			return new AwareBean();
		}

		@Bean(initMethodName = "init", destroyMethodName = "close", lazy = Lazy.TRUE)
		public AwareBean custom() {
			return new AwareBean();
		}
	}

	@Override
	protected void setUp() throws Exception {
		appCtx = new GenericApplicationContext();
		appCtx.registerShutdownHook();
		configurationProcessor = new ConfigurationProcessor(appCtx);

		AwareBean.DESTROYED = 0;
		AwareBean.INITIALIZED = 0;
		AwareBean.CUSTOM_INITIALIZED = 0;
		AwareBean.CUSTOM_DESTROYED = 0;
	}

	@Override
	protected void tearDown() throws Exception {
		configurationProcessor = null;
		if (appCtx != null && appCtx.isActive())
			appCtx.close();

	}

	public void testSimpleObject() throws Exception {

		assertEquals(0, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);

		// and do the processing
		configurationProcessor.processClass(Config.class);
		appCtx.refresh();

		assertEquals(3, appCtx.getBeanDefinitionCount());
		assertEquals(0, AwareBean.INITIALIZED);

		String name = "simple";
		AwareBean simple = (AwareBean) appCtx.getBean(name);
		assertNotNull(simple.getBeanFactory());

		assertEquals(1, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);

		assertSame(appCtx, simple.appCtx);
		assertEquals(name, simple.name);

		appCtx.close();
		assertEquals(1, AwareBean.DESTROYED);
	}

	public void testCustomMethods() throws Exception {

		assertEquals(0, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);

		assertEquals(0, AwareBean.CUSTOM_INITIALIZED);
		assertEquals(0, AwareBean.CUSTOM_DESTROYED);

		// and do the processing
		configurationProcessor.processClass(Config.class);
		appCtx.refresh();

		String name = "custom";

		assertEquals(0, AwareBean.CUSTOM_INITIALIZED);
		AwareBean custom = (AwareBean) appCtx.getBean(name);
		assertNotNull(custom.getBeanFactory());

		assertEquals(1, AwareBean.INITIALIZED);
		assertEquals(1, AwareBean.CUSTOM_INITIALIZED);
		assertEquals(0, AwareBean.CUSTOM_DESTROYED);

		assertSame(appCtx, custom.appCtx);
		assertEquals(name, custom.name);

		appCtx.close();
		assertEquals(1, AwareBean.CUSTOM_DESTROYED);
		assertEquals(1, AwareBean.DESTROYED);
	}

}
