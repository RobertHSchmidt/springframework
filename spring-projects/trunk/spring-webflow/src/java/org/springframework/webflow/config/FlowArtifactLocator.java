package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.access.ArtifactLookupException;

/**
 * A support interface used by FlowBuilders at configuration time to retrieve
 * dependent (but externally managed) flow artifacts neededed to build a flow
 * definition.
 * 
 * @author Keith Donald
 */
public interface FlowArtifactLocator {

	/**
	 * Retrieve the Flow to be used as a subflow by id.
	 * @param id the flow id
	 * @return the flow to be used as a subflow
	 * @throws ArtifactLookupException when no such flow is found
	 */
	public Flow getSubflow(String id) throws ArtifactLookupException;

	/**
	 * Retrieve the action with the provided id.
	 * @param id the id
	 * @return the action
	 * @throws ArtifactLookupException when no such mapper is found
	 */
	public Action getAction(String id) throws ArtifactLookupException;

	/**
	 * Retrieve the flow attribute mapper with the provided id.
	 * @param id the id
	 * @return the attribute mapper
	 * @throws ArtifactLookupException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws ArtifactLookupException;

	/**
	 * Retrieve the transition criteria with the provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws ArtifactLookupException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws ArtifactLookupException;

	/**
	 * Retrieve the view selector with the provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws ArtifactLookupException when no such creator is found
	 */
	public ViewSelector getViewSelector(String id) throws ArtifactLookupException;

	/**
	 * Retrieve the exception handler with the provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws ArtifactLookupException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws ArtifactLookupException;
}