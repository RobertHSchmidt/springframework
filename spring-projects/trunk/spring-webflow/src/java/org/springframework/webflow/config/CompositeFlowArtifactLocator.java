package org.springframework.webflow.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.access.ArtifactLookupException;
import org.springframework.webflow.access.NoSuchArtifactException;

/**
 * A flow artifact locator that queries an ordered chain of flow artifact
 * locators, stopping when one of those locators fulfills a request for an
 * artifact or the chain is exhausted and an ArtifactLookupException exception
 * is thrown.
 * @author Keith Donald
 */
public class CompositeFlowArtifactLocator implements FlowArtifactLocator {

	/**
	 * The artifact locator chain.
	 */
	public FlowArtifactLocator[] locatorChain;

	/**
	 * Creates a chained artifact locator that queries the specified locators in
	 * the order provided.
	 * @param locatorChain the artifact locator chain
	 */
	public CompositeFlowArtifactLocator(FlowArtifactLocator[] locatorChain) {
		Assert.notEmpty(locatorChain, "The artifact locator chain must have at least one element");
		this.locatorChain = locatorChain;
	}

	public Flow getSubflow(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getSubflow(id);
			}
			catch (NoSuchArtifactException e) {
				exceptions.add(e);
			}
		}
		throw new FlowArtifactLocatorChainExaustedException(Flow.class, id, exceptions);
	}

	public Action getAction(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getAction(id);
			}
			catch (NoSuchArtifactException e) {
				exceptions.add(e);
			}
		}
		throw new FlowArtifactLocatorChainExaustedException(Action.class, id, exceptions);
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getAttributeMapper(id);
			}
			catch (NoSuchArtifactException e) {

			}
		}
		throw new FlowArtifactLocatorChainExaustedException(FlowAttributeMapper.class, id, exceptions);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getTransitionCriteria(id);
			}
			catch (NoSuchArtifactException e) {
				exceptions.add(e);
			}
		}
		throw new FlowArtifactLocatorChainExaustedException(TransitionCriteria.class, id, exceptions);
	}

	public ViewSelector getViewSelector(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getViewSelector(id);
			}
			catch (NoSuchArtifactException e) {
				exceptions.add(e);
			}
		}
		throw new FlowArtifactLocatorChainExaustedException(ViewSelector.class, id, exceptions);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws ArtifactLookupException {
		List exceptions = new LinkedList();
		for (int i = 0; i < locatorChain.length; i++) {
			FlowArtifactLocator locator = locatorChain[i];
			try {
				return locator.getExceptionHandler(id);
			}
			catch (NoSuchArtifactException e) {
				exceptions.add(e);
			}
		}
		throw new FlowArtifactLocatorChainExaustedException(StateExceptionHandler.class, id, exceptions);
	}
}