package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;

/**
 * A flow artifact locator that pulls subflow definitions from a explict
 * {@link FlowRegistry} The rest of the artifacts ared sourced from a standard
 * Spring BeanFactory.
 * 
 * @see FlowRegistry
 * @see FlowArtifactFactory#getSubflow(String)
 * 
 * @author Keith Donald
 */
public class FlowRegistryFlowArtifactFactory extends BeanFactoryFlowArtifactFactory {

	/**
	 * The Spring bean factory.
	 */
	private FlowRegistry subflowRegistry;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 * @param subflowLocator The locator for loading subflows
	 */
	public FlowRegistryFlowArtifactFactory(FlowRegistry subflowRegistry, BeanFactory beanFactory) {
		super(beanFactory);
		this.subflowRegistry = subflowRegistry;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		return subflowRegistry.getFlow(id);
	}
}