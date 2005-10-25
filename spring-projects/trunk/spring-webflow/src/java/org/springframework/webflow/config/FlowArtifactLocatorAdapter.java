package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.access.FlowArtifactLookupException;

/**
 * Dummy implementation of a flow artifact locator that throws unsupported
 * operation exceptions for each lookup artifact. May be subclassed to offer
 * some lookup support.
 * 
 * @author Keith Donald
 */
public class FlowArtifactLocatorAdapter implements FlowArtifactLocator {

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Subflow lookup is not supported by this artifact locator");
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Action lookup is not supported by this artifact locator");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Attribute mapper lookup is not supported by this artifact locator");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Transition criteria lookup is not supported by this artifact locator");
	}

	public ViewDescriptorCreator getViewDescriptorCreator(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException(
				"View descriptor creator lookup is not supported by this artifact locator");
	}

}