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
package org.springframework.osgi.test.serviceproxy;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.service.support.collection.OsgiServiceCollection;
import org.springframework.osgi.test.ConfigurableBundleCreatorTests;

/**
 * @author Costin Leau
 * 
 */
public class ServiceCollectionTest extends ConfigurableBundleCreatorTests {

	protected String[] getBundleLocations() {
		return new String[] { localMavenArtifact("org.springframework.osgi", "aopalliance.osgi", "1.0-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "cglib-nodep.osgi", "2.1.3-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "spring-aop", "2.1-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "spring-beans", "2.1-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "spring-context", "2.1-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "spring-osgi-core", "1.0-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "spring-osgi-extender", "1.0-SNAPSHOT") };
	}

	protected String getManifestLocation() {
		return "org/springframework/osgi/test/serviceproxy/ServiceCollectionTest.MF";
	}

	private ServiceRegistration publishService(Object obj) throws Exception {
		return getBundleContext().registerService(obj.getClass().getName(), obj, null);
	}

	public void testCollectionListener() throws Exception {
		Collection collection = new OsgiServiceCollection(null, null, getBundleContext());

		ServiceReference[] refs = getBundleContext().getServiceReferences(null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();
		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
		}
		finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());
	}

	public void testCollectionContent() throws Exception {
		Collection collection = new OsgiServiceCollection(null, null, getBundleContext());
		ServiceReference[] refs = getBundleContext().getServiceReferences(null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();
		
		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
			// test service
			Iterator iter = collection.iterator();
			// reach our new service index
			for (int i = 0; i < size; i++) {
				iter.next();
			}
			Object myService = iter.next();
			// be sure to use classes loaded by the same CL
			System.out.println(myService);
			assertTrue(myService instanceof Date);
			assertEquals(time, ((Date) myService).getTime());
		}
		finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());

	}

}
