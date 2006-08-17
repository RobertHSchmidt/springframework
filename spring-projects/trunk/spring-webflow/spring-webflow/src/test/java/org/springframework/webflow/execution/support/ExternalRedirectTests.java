package org.springframework.webflow.execution.support;

import junit.framework.TestCase;

public class ExternalRedirectTests extends TestCase {

	private ExternalRedirect redirect;
	
	protected void setUp() throws Exception {
	}

	public void testStaticExpression() {
		redirect = new ExternalRedirect("my/url");
		assertEquals("my/url", redirect.getUrl());
	}
}