/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.rfc124.iandt.extender;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.Version;
import org.osgi.service.blueprint.context.ModuleContext;
import org.osgi.service.blueprint.context.ModuleContextListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.springframework.core.io.Resource;
import org.springframework.osgi.rfc124.iandt.BaseRFC124IntegrationTest;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * Integration test that checks basic behaviour signs of the RFC 124 Extender.
 * 
 * @author Costin Leau
 * 
 */
public class ExtenderBootstrapTest extends BaseRFC124IntegrationTest {

	private Bundle testBundle;
	private final Object monitor = new Object();


	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		installTestBundle();
	}

	@Override
	protected void onTearDown() throws Exception {
		if (testBundle != null) {
			testBundle.uninstall();
		}
	}

	public void testSanity() throws Exception {

		EventHandler handler = new EventHandler() {

			public void handleEvent(Event event) {
				System.out.println("Received event " + event);
			}
		};

		String[] topics = new String[] { "org/osgi/service/*" };
		Dictionary<String, Object> prop = new Hashtable<String, Object>();
		prop.put(EventConstants.EVENT_TOPIC, topics);
		bundleContext.registerService(EventHandler.class.getName(), handler, prop);

		testBundle.start();
		Thread.sleep(1000 * 3);
		testBundle.stop();
		Thread.sleep(1000 * 3);
	}

	public void testModuleContextService() throws Exception {
		installTestBundle();
		final boolean[] receviedEvent = new boolean[1];
		ServiceListener notifier = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				if (event.getServiceReference().getProperty(ModuleContext.SYMBOLIC_NAME_PROPERTY) != null) {
					logger.info("Found service "
							+ OsgiServiceReferenceUtils.getServicePropertiesSnapshotAsMap(event.getServiceReference()));

					synchronized (monitor) {
						receviedEvent[0] = true;
						monitor.notify();
					}
				}
			}
		};
		// wait for the service up to 3 minutes
		long waitTime = 2 * 60 * 1000;
		bundleContext.addServiceListener(notifier);

		testBundle.start();

		synchronized (monitor) {
			monitor.wait(waitTime);
			assertTrue(receviedEvent[0]);
		}
		bundleContext.removeServiceListener(notifier);

		testBundle.stop();
		assertNull("module context service should be unpublished",
			bundleContext.getServiceReference(ModuleContext.class.getName()));
	}

	public void testModuleContextListener() throws Exception {
		final List<String> contexts = new ArrayList<String>();
		ModuleContextListener listener = new ModuleContextListener() {

			public void contextCreated(String symName, Version version) {
				addToList(symName, version);
			}

			public void contextCreationFailed(String symName, Version version, Throwable ex) {
				addToList(symName, version);
			}

			private void addToList(String symName, Version version) {
				synchronized (contexts) {
					contexts.add(symName + "|" + version);
					contexts.notify();
				}
			}
		};

		installTestBundle();
		bundleContext.registerService(ModuleContextListener.class.getName(), listener, new Hashtable());

		testBundle.start();
		synchronized (contexts) {
			contexts.wait(2 * 1000 * 60);
			assertFalse("no event received", contexts.isEmpty());
		}
	}

	private void installTestBundle() throws Exception {
		Resource bundleResource = getLocator().locateArtifact("org.springframework.osgi.rfc124.iandt", "simple.bundle",
			getSpringDMVersion());
		testBundle = bundleContext.installBundle(bundleResource.getDescription(), bundleResource.getInputStream());
	}
}