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
import static org.springframework.config.java.test.Assert.assertBeanDefinitionCount;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Bean lifecycle (aware/init/destroy) tests
 *
 * @author Costin Leau
 * @author Chris Beams
 */
public class LifecycleTests {

	private ConfigurableJavaConfigApplicationContext ctx;

	@Before
	public void setUp() throws Exception {
		// TODO: change to JCAC (see todos below)
		ctx = new LegacyJavaConfigApplicationContext();
		ctx.registerShutdownHook();

		AwareBean.DESTROYED = 0;
		AwareBean.INITIALIZED = 0;
		AwareBean.CUSTOM_INITIALIZED = 0;
		AwareBean.CUSTOM_DESTROYED = 0;
	}

	@After
	public void tearDown() throws Exception {
		if (ctx != null && ctx.isActive())
			ctx.close();
	}

	// TODO: [lifecycle]
	public @Test void testSimpleObject() throws Exception {
		assertEquals(0, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);

		// do the processing
		ctx.addConfigClass(Config.class);
		ctx.refresh();

		assertBeanDefinitionCount(ctx, 3);
		assertEquals(0, AwareBean.INITIALIZED);

		String name = "simple";
		AwareBean simple = (AwareBean) ctx.getBean(name);
		assertNotNull(simple.getBeanFactory());

		assertEquals(1, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);

		assertSame(ctx, simple.appCtx);
		assertEquals(name, simple.name);

		ctx.close();
		assertEquals(1, AwareBean.DESTROYED);
	}
	// TODO: [lifecycle]
	public @Test void testCustomMethods() throws Exception {
		assertEquals(0, AwareBean.INITIALIZED);
		assertEquals(0, AwareBean.DESTROYED);
		assertEquals(0, AwareBean.CUSTOM_INITIALIZED);
		assertEquals(0, AwareBean.CUSTOM_DESTROYED);

		// and do the processing
		ctx.addConfigClass(Config.class);
		ctx.refresh();

		String name = "custom";

		assertEquals(0, AwareBean.CUSTOM_INITIALIZED);
		AwareBean custom = (AwareBean) ctx.getBean(name);
		assertNotNull(custom.getBeanFactory());

		assertEquals(1, AwareBean.INITIALIZED);
		assertEquals(1, AwareBean.CUSTOM_INITIALIZED);
		assertEquals(0, AwareBean.CUSTOM_DESTROYED);

		assertSame(ctx, custom.appCtx);
		assertEquals(name, custom.name);

		ctx.close();
		assertEquals(1, AwareBean.CUSTOM_DESTROYED);
		assertEquals(1, AwareBean.DESTROYED);
	}
	public static class Config {
		@Bean(lazy = Lazy.TRUE)
		public AwareBean simple() { return new AwareBean(); }

		@Bean(initMethodName = "init", destroyMethodName = "close", lazy = Lazy.TRUE)
		public AwareBean custom() { return new AwareBean(); }
	}


	private static class AwareBean implements InitializingBean, DisposableBean, BeanFactoryAware,
	                                          BeanNameAware, ApplicationContextAware {
		static int DESTROYED = 0;
		static int INITIALIZED = 0;
		static int CUSTOM_DESTROYED = 0;
		static int CUSTOM_INITIALIZED = 0;

		BeanFactory factory = null;
		String name;
		ApplicationContext appCtx;

		public void destroy() throws Exception { DESTROYED++; }
		public void afterPropertiesSet() throws Exception { INITIALIZED++; }
		public void close() { CUSTOM_DESTROYED++; }
		public void init() { CUSTOM_INITIALIZED++; }

		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.factory = beanFactory;
		}
		public BeanFactory getBeanFactory() { return factory; }

		public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
			this.appCtx = appCtx;
		}

		public void setBeanName(String name) {
			this.name = name;
		}
	}

}
