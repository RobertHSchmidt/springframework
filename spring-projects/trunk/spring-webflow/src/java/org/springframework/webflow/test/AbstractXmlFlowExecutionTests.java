package org.springframework.webflow.test;

import org.springframework.core.io.Resource;
import org.springframework.webflow.registry.XmlFlowRegistrar;

public abstract class AbstractXmlFlowExecutionTests extends AbstractRegisteredFlowExecutionTests {

	protected void populateFlowRegistry() { 
		XmlFlowRegistrar registrar = new XmlFlowRegistrar();
		registrar.addFlowLocation(getFlowLocation());
		registrar.addFlowLocations(getSubflowLocations());
		registrar.registerFlows(getFlowRegistry(), getFlowArtifactFactory());
	} 
	
	/**
	 * Returns the array of resources pointing to the XML-based flow definitions
	 * needed by this flow execution test: subclasses must override.
	 * <p>
	 * Flow definitions stored in the returned resouce array are automatically
	 * added to this test's FlowRegistry by the {@link #createFlowLocator}
	 * method, called on test setup.
	 * @return the locations of the XML flow definitions needed by this test
	 */
	protected abstract Resource getFlowLocation();

	/**
	 * Returns the array of resources pointing to the XML-based flow definitions
	 * needed by this flow execution test: subclasses must override.
	 * <p>
	 * Flow definitions stored in the returned resouce array are automatically
	 * added to this test's FlowRegistry by the {@link #createFlowLocator}
	 * method, called on test setup.
	 * @return the locations of the XML flow definitions needed by this test
	 */
	protected Resource[] getSubflowLocations() {
		return null;
	}	
}