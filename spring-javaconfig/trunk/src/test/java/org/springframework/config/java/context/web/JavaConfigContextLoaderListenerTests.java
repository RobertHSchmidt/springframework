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
package org.springframework.config.java.context.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;
import static org.springframework.web.context.ContextLoader.CONFIG_LOCATION_PARAM;
import static org.springframework.web.context.ContextLoader.CONTEXT_CLASS_PARAM;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;
import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

import javax.servlet.ServletContextEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class JavaConfigContextLoaderListenerTests {

	private JavaConfigContextLoaderListener listener;

	private MockServletContext servletContext;

	@Before
	public void setUp() {
		listener = new JavaConfigContextLoaderListener();
		servletContext = new MockServletContext();
	}

	@After
	public void commonValidation() {
		WebApplicationContext appContext = getWebApplicationContext(servletContext);
		assumeNotNull(appContext);

		if (!(appContext instanceof JavaConfigWebApplicationContext))
			return;

		assertThat(appContext.getBean("foo"), is(TestBean.class));
	}

	@Test
	public void testTypicalUsage() {
		servletContext.addInitParameter(CONFIG_LOCATION_PARAM, SimpleRootConfig.class.getName());
		listener.contextInitialized(new ServletContextEvent(servletContext));
		getRequiredWebApplicationContext(servletContext);
	}

	/**
	 * Required to run this test under Junit3 because the way exceptions are
	 * thrown from listener confuses junit4
	 */
	public static class Junit3Tests extends junit.framework.TestCase {
		public void testFailingToProvideConfigLocationThrowsException() {
			JavaConfigContextLoaderListener listener = new JavaConfigContextLoaderListener();
			MockServletContext servletContext = new MockServletContext();

			Throwable ex = null;

			try {
				listener.contextInitialized(new ServletContextEvent(servletContext));
			}
			catch (Throwable t) {
				ex = t;
			}

			assertThat(ex, is(IllegalArgumentException.class));
		}
	}

	@Test
	public void testExplictlySupplyingJavaConfigContextClassInitParam() {
		servletContext.addInitParameter(CONTEXT_CLASS_PARAM, JavaConfigWebApplicationContext.class.getName());
		servletContext.addInitParameter(CONFIG_LOCATION_PARAM, SimpleRootConfig.class.getName());

		listener.contextInitialized(new ServletContextEvent(servletContext));

		getRequiredWebApplicationContext(servletContext);
	}

	@Test
	public void testSupplyingAlternateContextClassInitParam() {
		servletContext.addInitParameter(CONTEXT_CLASS_PARAM, XmlWebApplicationContext.class.getName());
		servletContext.addInitParameter(CONFIG_LOCATION_PARAM,
				"classpath:org/springframework/config/java/context/simpleConfiguration.xml");

		listener.contextInitialized(new ServletContextEvent(servletContext));

		assertThat(getWebApplicationContext(servletContext), is(XmlWebApplicationContext.class));
	}

	@Configuration
	static class SimpleRootConfig {
		@Bean
		public TestBean foo() {
			return new TestBean();
		}
	}
}
