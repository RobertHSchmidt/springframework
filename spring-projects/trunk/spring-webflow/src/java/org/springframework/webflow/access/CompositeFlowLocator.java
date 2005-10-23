package org.springframework.webflow.access;

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * Delegates a request to locate a Flow definition by ID to a ordered chain of
 * FlowLocators. Iterates through the list until the Flow definition is found or
 * the chain is exhausted and a NoSuchFlowDefinition exception is thrown.
 * @author Keith Donald
 */
public class CompositeFlowLocator implements FlowLocator {

	/**
	 * The FlowLocator chain.
	 */
	private FlowLocator[] locators;

	/**
	 * Creates a chained flow locator made from the list of specified locators.
	 * @param locators The FlowLocator list
	 */
	public CompositeFlowLocator(FlowLocator[] locators) {
		Assert.notEmpty(locators, "The flow locator chain cannot be empty");
		this.locators = locators;
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < locators.length; i++) {
			try {
				return locators[i].getFlow(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new NoSuchFlowDefinitionException(id);
	}
}