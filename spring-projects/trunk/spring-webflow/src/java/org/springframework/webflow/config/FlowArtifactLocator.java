package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.access.FlowArtifactLookupException;

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
	 * @throws FlowArtifactLookupException when no such flow is found
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
	 * @return the attribute mapper
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the transition criteria with the provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws FlowArtifactLookupException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the view selector with the provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws FlowArtifactLookupException when no such creator is found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the exception handler with the provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws FlowArtifactLookupException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException;
}