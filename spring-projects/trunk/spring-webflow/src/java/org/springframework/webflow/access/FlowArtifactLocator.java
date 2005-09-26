package org.springframework.webflow.access;

import org.springframework.webflow.FlowAttributeMapper;

/**
 * A helper interface used by FlowBuilders at configuration time to retrieve
 * neccessary flow artifacts to build a Flow definition.
 * @author Keith
 */
public interface FlowArtifactLocator extends FlowLocator, ActionLocator {

	/**
	 * Retrieve the flow attribute mapper with the provided id.
	 * @param id the id
	 * @return the mapper
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public FlowAttributeMapper getFlowAttributeMapper(String id) throws FlowArtifactLookupException;
}
