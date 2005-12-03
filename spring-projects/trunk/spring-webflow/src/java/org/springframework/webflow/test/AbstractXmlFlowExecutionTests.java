package org.springframework.webflow.test;

import org.springframework.core.io.Resource;
import org.springframework.webflow.registry.XmlFlowRegistrar;

/**
 * Base class for flow integration tests that verify a XML flow definition executes
 * as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractXmlFlowExecutionTests extends AbstractFlowRegistryFlowExecutionTests {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.webflow.test.AbstractRegisteredFlowExecutionTests#populateFlowRegistry()
	 */
	protected void populateFlowRegistry() {
		XmlFlowRegistrar registrar = new XmlFlowRegistrar();
		registrar.addFlowLocation(getFlowLocation());
		registrar.addFlowLocations(getSubflowLocations());
		registrar.registerFlows(getFlowRegistry(), getFlowArtifactFactory());
	}
	
	/**
	 * Returns the resource pointing to the XML-based flow definition needed by
	 * this flow execution test: subclasses must override.
	 * <p>
	 * The Flow definitions store returned is automatically added to this test's
	 * FlowRegistry by the {@link #populateFlowRegistry} method, called n test
	 * setup.
	 * @return the location of the XML flow definition to test
	 */
	protected abstract Resource getFlowLocation();

	/**
	 * Returns the array of resources pointing to the XML-based subflow
	 * definitions needed by this flow execution test.  Optional.
	 * <p>
	 * Flow definitions stored in the returned resouce array are automatically
	 * added to this test's FlowRegistry by the {@link #populateFlowRegistry}
	 * method, called on test setup.
	 * @return the locations of the XML flow definitions needed as subflows by
	 * this test
	 */
	protected Resource[] getSubflowLocations() {
		return null;
	}
}