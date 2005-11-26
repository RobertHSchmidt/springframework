package org.springframework.webflow.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;

/**
 * A support interface used by FlowBuilders at configuration time to retrieve
 * dependent (but externally managed) flow artifacts needed to build a flow
 * definition.
 * 
 * @author Keith Donald
 */
public interface FlowArtifactFactory {

	/**
	 * Retrieve the Flow to be used as a subflow with the provided id.
	 * @param id the flow id
	 * @return the flow to be used as a subflow
	 * @throws FlowArtifactLookupException when no such flow is found
	 */
	public Flow getSubflow(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the action to be executed within a flow with the provided id.
	 * @param id the id
	 * @return the action
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public Action getAction(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the flow attribute mapper to be used in a subflow state with the
	 * provided id.
	 * @param id the id
	 * @return the attribute mapper
	 * @throws FlowArtifactLookupException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the transition criteria to drive state transitions with the
	 * provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws FlowArtifactLookupException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the view selector to make view selections in view states with
	 * the provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws FlowArtifactLookupException when no such creator is found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the exception handler to handle state exceptions with the
	 * provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws FlowArtifactLookupException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve the transition target state resolver with the specified id.
	 * @param id the id
	 * @return the target state resolver
	 * @throws FlowArtifactLookupException when no such handler is found
	 */
	public Transition.TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve a new flow definition instance with the specified id.
	 * @param id the id
	 * @return the new flow definition
	 */
	public Flow createFlow(String id) throws FlowArtifactLookupException;

	/**
	 * Retrieve a new state definition instance with the specified id.
	 * @param id the id
	 * @param stateType the state type
	 * @return the state
	 */
	public State createState(String id, Class stateType) throws FlowArtifactLookupException;

	/**
	 * Retrieve a new state transition instance with the specified id.
	 * @param id the id
	 * @return the transition
	 */
	public Transition createTransition(String id) throws FlowArtifactLookupException;

	/**
	 * Returns a generic bean (service) registry, for accessing arbitrary beans.
	 * 
	 * @return the generic service registry
	 */
	public BeanFactory getServiceRegistry() throws UnsupportedOperationException;

}