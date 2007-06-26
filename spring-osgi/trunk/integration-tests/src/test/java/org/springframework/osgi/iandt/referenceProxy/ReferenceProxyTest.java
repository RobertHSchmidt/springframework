package org.springframework.osgi.iandt.referenceProxy;

import org.osgi.framework.Bundle;
import org.springframework.osgi.ServiceUnavailableException;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.iandt.reference.proxy.ServiceReferer;
import org.springframework.osgi.iandt.simpleservice.MyService;

/**
 * @author Hal Hildebrand Date: Nov 25, 2006 Time: 12:42:30 PM
 */
public class ReferenceProxyTest extends AbstractConfigurableBundleCreatorTests {

	protected String getManifestLocation() {
		// return
		// "classpath:org/springframework/osgi/test/referenceProxy/ReferenceProxyTest.MF";
		return null;
	}

	protected String[] getBundles() {
		return new String[] {
				localMavenArtifact("org.springframework.osgi", "commons-collections.osgi", "3.2-SNAPSHOT"),
				localMavenArtifact("org.springframework.osgi", "org.springframework.osgi.iandt.simple.service",
					getSpringOsgiVersion()),
				localMavenArtifact("org.springframework.osgi", "org.springframework.osgi.iandt.reference.proxy",
					getSpringOsgiVersion()) };
	}

	public void testReferenceProxyLifecycle() throws Exception {
	
		MyService reference = ServiceReferer.serviceReference;
		
		assertNotNull("reference not initialized", reference);
		assertNotNull("no value specified in the reference", reference.stringValue());

		Bundle simpleServiceBundle = findBundleBySymbolicName("org.springframework.osgi.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);
		System.out.println("stopping bundle");
		simpleServiceBundle.stop();

		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			System.out.println("waiting for bundle to stop");
			Thread.sleep(10);
		}
		System.out.println("bundle stopped");

		// Service should be unavailable
		try {
			reference.stringValue();
			fail("ServiceUnavailableException should have been thrown!");
		}
		catch (ServiceUnavailableException e) {
			// Expected
		}

		System.out.println("starting bundle");
		simpleServiceBundle.start();

		waitOnContextCreation("org.springframework.osgi.iandt.simpleservice");

		System.out.println("bundle started");
		// Service should be running
		assertNotNull(reference.stringValue());
	}

	/* (non-Javadoc)
	 * @see org.springframework.osgi.test.AbstractSynchronizedOsgiTests#getDefaultWaitTime()
	 */
	protected long getDefaultWaitTime() {
		return 15L;
	}
	


	protected String getPlatformName() {
		return KNOPFLERFISH_PLATFORM;
	}

}
