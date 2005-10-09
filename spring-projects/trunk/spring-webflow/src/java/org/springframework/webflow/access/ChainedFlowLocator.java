package org.springframework.webflow.access;

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * Delegates a request to locate a Flow definition by ID to a ordered chain of
 * FlowLocators. Iterates through the list until the Flow definition is found or
 * the chain is exhausted.
 * @author Keith Donald
 */
public class ChainedFlowLocator implements FlowLocator {

	/**
	 * The FlowLocator chain.
	 */
	private FlowLocator[] chain;

	/**
	 * Creates a chained flow locator made from the list of specified locators.
	 * @param flowLocator The FlowLocator list
	 */
	public ChainedFlowLocator(FlowLocator[] chain) {
		Assert.notEmpty(chain, "The chain cannot be empty");
		this.chain = chain;
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowLocator locator = chain[i];
			try {
				return locator.getFlow(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new NoSuchFlowDefinitionException(id);
	}
}