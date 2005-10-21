package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.access.FlowArtifactLookupException;

/**
 * A helper interface used by FlowBuilders at configuration time to retrieve
 * necessary flow artifacts to build a single Flow definition from a factory.
 * @author Keith
 */
public interface FlowArtifactLocator {

	/**
	 * Retrieve the flow locator that will locate subflows.
	 * @param id the id
	 * @return the flow to be used as a subflow
	 * @throws FlowArtifactLookupException when no such subflow is found
	 */
	public Flow getSubflow(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the action with the provided id.
	 * @param id the id
	 * @return the action
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public Action getAction(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the flow attribute mapper with the provided id.
	 * @param id the id
	 * @return the mapper
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException;

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