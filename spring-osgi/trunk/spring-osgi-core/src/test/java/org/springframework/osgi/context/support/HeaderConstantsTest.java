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
package org.springframework.osgi.context.support;

import java.util.Dictionary;
import java.util.Properties;

import org.springframework.osgi.util.ConfigUtils;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class HeaderConstantsTest extends TestCase {

	private Dictionary headers;

	private String springCtxHeader;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		headers = new Properties();
		headers.put("some key", new Object());
		headers.put("another header", new Object());
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER + "1", new Object());
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		headers = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getSpringContextHeader(java.util.Dictionary)}.
	 */
	public void testGetServiceContextHeader() {
		assertNull(ConfigUtils.getSpringContextHeader(headers));
		String headerValue = "correct header";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, headerValue);
		assertSame(headerValue, ConfigUtils.getSpringContextHeader(headers));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getDirectiveValue(java.lang.String, java.lang.String)}.
	 */
	public void testGetDirectiveValue() {
		String header = "bla bla";
		assertNull(ConfigUtils.getDirectiveValue(header, "bla"));
		assertNull(ConfigUtils.getDirectiveValue(header, header));
		String directive = "directive";
		String value = "value";
		header = directive + ConfigUtils.EQUALS + value;
		assertEquals(value, ConfigUtils.getDirectiveValue(header, directive));
		assertNull(ConfigUtils.getDirectiveValue(header, value));
		assertNull(ConfigUtils.getDirectiveValue(header, "directiv"));
		header = directive + ConfigUtils.EQUALS + value + ConfigUtils.EQUALS + value;
		assertNull(ConfigUtils.getDirectiveValue(header, directive));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getDontPublishContext(java.util.Dictionary)}.
	 */
	public void testGetDontPublishContext() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_DONT_PUBLISH + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_DONT_PUBLISH + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_DONT_PUBLISH + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getEagerlyInitializeImporters(java.util.Dictionary)}.
	 */
	public void testGetEagerlyInitializeImporters() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_EAGERLY_INIT_IMPORTERS + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getEagerlyInitializeImporters(headers));

		header = ConfigUtils.DIRECTIVE_EAGERLY_INIT_IMPORTERS + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getEagerlyInitializeImporters(headers));

		header = ConfigUtils.DIRECTIVE_EAGERLY_INIT_IMPORTERS + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getEagerlyInitializeImporters(headers));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getCreateAsync(java.util.Dictionary)}.
	 */
	public void testGetCreateAsync() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getCreateAsync(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getCreateAsync(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getCreateAsync(headers));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.util.ConfigUtils#getWaitForDependencies(java.util.Dictionary)}.
	 */
	public void testGetWaitForDependencies() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getDontPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_WAIT_FOR_DEPENDENCIES + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getWaitForDependencies(headers));

		header = ConfigUtils.DIRECTIVE_WAIT_FOR_DEPENDENCIES + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getWaitForDependencies(headers));

		header = ConfigUtils.DIRECTIVE_WAIT_FOR_DEPENDENCIES + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getWaitForDependencies(headers));
	}

}
