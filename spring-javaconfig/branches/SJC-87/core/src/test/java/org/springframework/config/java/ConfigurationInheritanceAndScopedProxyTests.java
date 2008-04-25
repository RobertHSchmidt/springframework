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

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.LegacyJavaConfigApplicationContext;
import org.springframework.config.java.support.ConfigurationSupport;

/**
 * Corners bug SJC-25 which prohibited overriding {@link ScopedProxy}
 * {@link Bean} methods.
 *
 * @author Guillaume Duchesneau
 * @author Chris Beams
 */
public class ConfigurationInheritanceAndScopedProxyTests {

	public static final String SCOPE = "my scope";
	private ConfigurableJavaConfigApplicationContext ctx;
	private CustomScope customScope = new CustomScope();

	@Before
	public void setUp() throws Exception {
		ctx = new LegacyJavaConfigApplicationContext(ExtendedConfigurationClass.class) {
			@Override
			protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
				super.customizeBeanFactory(beanFactory);
				beanFactory.registerScope(SCOPE, customScope);
			}
		};
	}
	public static abstract class BaseConfigurationClass extends ConfigurationSupport {
		@ScopedProxy @Bean(scope = SCOPE)
		public TestBean overridenTestBean() {
			TestBean tb = new TestBean();
			tb.setName("overridenTestBean");
			return tb;
		}
		@ScopedProxy @Bean(scope = SCOPE)
		public abstract TestBean abstractTestBean();
	}
	public static class ExtendedConfigurationClass extends BaseConfigurationClass {
		@Override
		public TestBean overridenTestBean() {
			TestBean tb = super.overridenTestBean();
			tb.setName(tb.getName() + "-modified");
			return tb;
		}
		@Override
		public TestBean abstractTestBean() { return new TestBean("abstractTestBean"); }
	}


	// TODO: [@ScopedProxy]
	public @Test void testConfigurationInheritance() {
		TestBean overridenTestBean = ctx.getBean(TestBean.class, "overridenTestBean");
		assertNotNull(overridenTestBean);
		assertEquals("overridenTestBean-modified", overridenTestBean.getName());

		TestBean abstractTestBean = ctx.getBean(TestBean.class, "abstractTestBean");
		assertNotNull(abstractTestBean);
		assertEquals("abstractTestBean", abstractTestBean.getName());

		customScope.createNewScope = true;

		TestBean overridenTestBean2 = ctx.getBean(TestBean.class, "overridenTestBean");
		assertNotNull(overridenTestBean2);
		assertEquals("overridenTestBean-modified", overridenTestBean2.getName());
		assertNotSame(overridenTestBean, overridenTestBean2);

		TestBean abstractTestBean2 = ctx.getBean(TestBean.class, "abstractTestBean");
		assertNotNull(abstractTestBean2);
		assertEquals("abstractTestBean", abstractTestBean2.getName());
		assertNotSame(abstractTestBean, abstractTestBean2);
	}
}
