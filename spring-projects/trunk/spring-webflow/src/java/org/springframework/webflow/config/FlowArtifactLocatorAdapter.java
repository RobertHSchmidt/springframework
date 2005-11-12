package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.access.ArtifactLookupException;
import org.springframework.webflow.access.FlowLocator;

/**
 * Dummy implementation of a flow artifact locator that throws unsupported
 * operation exceptions for each lookup artifact. May be subclassed to offer
 * some lookup support.
 * 
 * @author Keith Donald
 */
public class FlowArtifactLocatorAdapter implements FlowArtifactLocator {

	/**
	 * The flow locator delegate (may be <code>null</code>).
	 */
	private FlowLocator flowLocator;

	/**
	 * Creates an artifact locator adapter that does not natively support any
	 * artifact lookup operations.
	 */
	public FlowArtifactLocatorAdapter() {

	}

	/**
	 * Creates an artifact locator adapter that delegates to the provided 
	 * flow locator for subflow resolution.
	 * @param flowLocator the flow locator (may be <code>null</code).
	 */
	public FlowArtifactLocatorAdapter(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	public Flow getSubflow(String id) throws ArtifactLookupException {
		if (flowLocator != null) {
			return flowLocator.getFlow(id);
		}
		else {
			throw new UnsupportedOperationException("Subflow lookup is not supported by this artifact locator");
		}
	}

	public Action getAction(String id) throws ArtifactLookupException {
		throw new UnsupportedOperationException("Action lookup is not supported by this artifact locator");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws ArtifactLookupException {
		throw new UnsupportedOperationException("Attribute mapper lookup is not supported by this artifact locator");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws ArtifactLookupException {
		throw new UnsupportedOperationException("Transition criteria lookup is not supported by this artifact locator");
	}

	public ViewSelector getViewSelector(String id) throws ArtifactLookupException {
		throw new UnsupportedOperationException(
				"View descriptor creator lookup is not supported by this artifact locator");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws ArtifactLookupException {
		throw new UnsupportedOperationException(
				"Flow exception handler lookup is not supported by this artifact locator");
	}
}