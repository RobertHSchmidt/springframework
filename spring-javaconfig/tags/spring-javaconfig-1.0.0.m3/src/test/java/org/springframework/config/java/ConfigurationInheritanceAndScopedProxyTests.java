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
import org.springframework.config.java.process.ConfigurationProcessor;
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

	public static String flag = "1";

	private DefaultListableBeanFactory bf;

	private ConfigurationProcessor configurationProcessor;

	private CustomScope customScope = new CustomScope();

	@Before
	public void setUp() throws Exception {
		bf = new DefaultListableBeanFactory();
		bf.registerScope(SCOPE, customScope);
		configurationProcessor = new ConfigurationProcessor(bf);

		// and do the processing
		configurationProcessor.processClass(ExtendedConfigurationClass.class);
	}

	public static abstract class BaseConfigurationClass extends ConfigurationSupport {

		@Bean(scope = SCOPE)
		@ScopedProxy
		public TestBean overridenTestBean() {
			TestBean tb = new TestBean();
			tb.setName("overridenTestBean");
			return tb;
		}

		@Bean(scope = SCOPE)
		@ScopedProxy
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
		public TestBean abstractTestBean() {
			TestBean tb = new TestBean();
			tb.setName("abstractTestBean");
			return tb;
		}
	}

	@Test
	public void testConfigurationInheritance() {

		TestBean overridenTestBean = (TestBean) bf.getBean("overridenTestBean");
		assertNotNull(overridenTestBean);
		assertEquals("overridenTestBean-modified", overridenTestBean.getName());

		TestBean abstractTestBean = (TestBean) bf.getBean("abstractTestBean");
		assertNotNull(abstractTestBean);
		assertEquals("abstractTestBean", abstractTestBean.getName());

		customScope.createNewScope = true;

		TestBean overridenTestBean2 = (TestBean) bf.getBean("overridenTestBean");
		assertNotNull(overridenTestBean2);
		assertEquals("overridenTestBean-modified", overridenTestBean2.getName());
		assertNotSame(overridenTestBean, overridenTestBean2);

		TestBean abstractTestBean2 = (TestBean) bf.getBean("abstractTestBean");
		assertNotNull(abstractTestBean2);
		assertEquals("abstractTestBean", abstractTestBean2.getName());
		assertNotSame(abstractTestBean, abstractTestBean2);
	}
}
