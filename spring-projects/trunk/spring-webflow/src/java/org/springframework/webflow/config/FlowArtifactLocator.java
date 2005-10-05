package org.springframework.webflow.config;

import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.access.ActionLocator;
import org.springframework.webflow.access.FlowArtifactLookupException;
import org.springframework.webflow.access.FlowLocator;

/**
 * A helper interface used by FlowBuilders at configuration time to retrieve
 * necessary flow artifacts to build a Flow definition from a factory.
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

	/**
	 * Retrieve the transition criteria with the provided id.
	 * @param id the id
	 * @return the criteria
	 * @throws FlowArtifactLookupException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the view descriptor creator with the provided id.
	 * @param id the id
	 * @return the creator
	 * @throws FlowArtifactLookupException when no such creator is found
	 */
	public ViewDescriptorCreator getViewDescriptorCreator(String id) throws FlowArtifactLookupException;
}