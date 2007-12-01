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

import static org.junit.Assert.assertEquals;

import javax.servlet.ServletException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class JavaConfigDispatcherServletTest {

	private final JavaConfigDispatcherServlet servlet = new JavaConfigDispatcherServlet();

	@Test
	public void testGetContextClass() {
		assertEquals(JavaConfigWebApplicationContext.class, servlet.getContextClass());
	}

	// TODO: currently does not support overriding the contextClass.
	@Ignore
	@Test
	public void testUserSpecifiedContextClassOverridesDefault() throws ServletException {
		MockServletContext servletContext = new MockServletContext();
		servletContext.addInitParameter("contextClass", ClassPathXmlApplicationContext.class.getName());
		servletContext.addInitParameter("configLocations", "foo");
		servlet.init(new MockServletConfig(servletContext));

		assertEquals(ClassPathXmlApplicationContext.class, servlet.getContextClass());
	}
}
