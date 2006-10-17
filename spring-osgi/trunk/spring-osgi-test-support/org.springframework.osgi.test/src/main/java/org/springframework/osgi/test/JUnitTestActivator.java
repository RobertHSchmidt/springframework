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
package org.springframework.osgi.test;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.test.runner.TestRunner;

/**
 * Test bundle activator - looks for a predefined JUnit test runner and triggers
 * the test execution.
 * 
 * @author Costin Leau
 * 
 */
public class JUnitTestActivator implements BundleActivator {

	private BundleContext context;
	private ServiceReference reference;
	private ServiceRegistration registration;
	private TestRunner service;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		this.context = bc;

		reference = context.getServiceReference(TestRunner.class.getName());
		if (reference == null)
			throw new IllegalArgumentException("cannot find service at " + TestRunner.class.getName());
		service = (TestRunner) context.getService(reference);

		registration = context.registerService(JUnitTestActivator.class.getName(), this, new Hashtable());

	}

	public void executeTest() {
		service.runTest(loadTest());
	}

	private OsgiJUnitTest loadTest() {
		String testClass = System.getProperty(OsgiJUnitTest.OSGI_TEST);
		if (testClass == null)
			throw new IllegalArgumentException("no test class specified under " + OsgiJUnitTest.OSGI_TEST);

		try {
			// use bundle to load the classes
			Class clazz = context.getBundle().loadClass(testClass);
			OsgiJUnitTest test = (OsgiJUnitTest) clazz.newInstance();
			test.setBundleContext(context);
			return test;

		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		bc.ungetService(reference);
		if (registration != null)
			registration.unregister();
	}

}
