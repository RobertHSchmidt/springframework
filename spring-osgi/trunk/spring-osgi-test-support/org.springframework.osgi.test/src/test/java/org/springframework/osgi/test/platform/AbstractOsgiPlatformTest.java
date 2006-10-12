/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.osgi.test.platform;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;

/**
 * @author Costin Leau
 * 
 */
public abstract class AbstractOsgiPlatformTest extends TestCase {

	OsgiPlatform platform;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		platform = createOsgiPlatform();
		platform.start();
	}

	protected abstract OsgiPlatform createOsgiPlatform();

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		platform.stop();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.EquinoxPlatform#start()}.
	 */
	public void testEquinoxPlatform() throws Exception {
		BundleContext context = platform.getBundleContext();
		assertNotNull(context);
		assertCorrectPlatform(context);
	}

	protected abstract void assertCorrectPlatform(BundleContext context);
}
