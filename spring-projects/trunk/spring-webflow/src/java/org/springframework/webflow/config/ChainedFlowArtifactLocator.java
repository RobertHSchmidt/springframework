package org.springframework.webflow.config;

import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.access.FlowArtifactLookupException;

/**
 * A flow artifact locator that queries an ordered chain of flow artifact
 * locators, stopping when one of those locators fulfills a request for an
 * artifact or the chain is exhausted and an ArtifactLookupException exception is
 * thrown.
 * @author Keith Donald
 */
public class ChainedFlowArtifactLocator implements FlowArtifactLocator {

	/**
	 * The artifact locator chain.
	 */
	public FlowArtifactLocator[] chain;

	/**
	 * Creates a chained artifact locator that queries the specified locators in
	 * the order provided.
	 * @param chain the artifact locator chain
	 */
	public ChainedFlowArtifactLocator(FlowArtifactLocator[] chain) {
		Assert.notEmpty(chain, "The artifact locator chain must have at least one element");
		this.chain = chain;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getSubflow(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(Flow.class, id, "Chain exhausted looking for flow with id: '" + id + "'");
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getAction(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(Action.class, id, "Chain exhausted looking for Action with id: '" + id + "'");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getAttributeMapper(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(FlowAttributeMapper.class, id,
				"Chain exhausted looking for attribute mapper with id: '" + id + "'");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getTransitionCriteria(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(TransitionCriteria.class, id,
				"Chain exhausted looking for transition criteria with id: '" + id + "'");
	}
	
	public ViewDescriptorCreator getViewDescriptorCreator(String id) throws FlowArtifactLookupException {
		for (int i = 0; i < chain.length; i++) {
			FlowArtifactLocator locator = chain[i];
			try {
				return locator.getViewDescriptorCreator(id);
			}
			catch (FlowArtifactLookupException e) {

			}
		}
		throw new FlowArtifactLookupException(ViewDescriptorCreator.class, id,
				"Chain exhausted looking for view descriptor creator with id: '" + id + "'");
	}

}