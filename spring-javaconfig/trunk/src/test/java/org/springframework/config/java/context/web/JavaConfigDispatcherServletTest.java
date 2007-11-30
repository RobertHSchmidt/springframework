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
