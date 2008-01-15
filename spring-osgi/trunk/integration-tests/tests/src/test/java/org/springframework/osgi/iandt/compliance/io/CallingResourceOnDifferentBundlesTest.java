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

package org.springframework.osgi.iandt.compliance.io;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * 
 * http://sourceforge.net/tracker/index.php?func=detail&aid=1581187&group_id=82798&atid=567241
 * ClassCastException from Bundle.getResource when called on Bundle passed to
 * the caller
 * 
 * 
 * @author Costin Leau
 * 
 */
public class CallingResourceOnDifferentBundlesTest extends BaseIntegrationTest {

	private static final String LOCATION = "META-INF/";


	public void testCallGetResourceOnADifferentBundle() throws Exception {
		// find bundles
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 1; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			logger.debug("calling #getResource on bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
			URL url = bundle.getResource(LOCATION);
			if (!OsgiBundleUtils.isFragment(bundle))
				assertNotNull(url);
		}
	}

	public void testCallGetResourcesOnADifferentBundle() throws Exception {
		// find bundles
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 1; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			logger.debug("calling #getResources on bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
			Enumeration enm = bundle.getResources(LOCATION);
			if (!OsgiBundleUtils.isFragment(bundle))
				assertNotNull(enm);
		}

	}

	public void testCallGetResourceOnADifferentBundleRetrievedThroughBundleEvent() throws Exception {
		String CGLIB_BUNDLE_NAME = "spring-core";

		Bundle[] bundles = bundleContext.getBundles();
		Bundle bundle = null;
		// find cglib library as we don't use it
		for (int i = 1; bundle == null && i < bundles.length; i++) {
			if (CGLIB_BUNDLE_NAME.equals(OsgiStringUtils.nullSafeName(bundles[i])))
				bundle = bundles[i];
		}

		assertNotNull("no bundle found", bundle);
		final Bundle sampleBundle = bundle;

		final boolean[] listenerCalled = new boolean[] { false };

		// register listener
		bundleContext.addBundleListener(new SynchronousBundleListener() {

			public void bundleChanged(BundleEvent event) {
				// call getResource
				event.getBundle().getResource(LOCATION);
				// call getResources
				try {
					event.getBundle().getResources(LOCATION);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}

				listenerCalled[0] = true;
			}
		});

		// update
		sampleBundle.stop();

		assertTrue("bundle listener hasn't been called", listenerCalled[0]);
	}
}
