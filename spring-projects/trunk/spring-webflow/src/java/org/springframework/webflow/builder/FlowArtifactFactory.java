package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewSelector;

/**
 * A support interface used by FlowBuilders at configuration time to retrieve
 * dependent (but externally managed) flow artifacts needed to build a flow
 * definition. Acts a facade to accessing externally managed flow artifacts.
 * 
 * @author Keith Donald
 */
public interface FlowArtifactFactory {

	/**
	 * Retrieve the Flow to be used as a subflow with the provided id.
	 * @param id the flow id
	 * @return the flow to be used as a subflow
	 * @throws FlowArtifactException when no such flow is found
	 */
	public Flow getSubflow(String id) throws FlowArtifactException;

	/**
	 * Retrieve the action to be executed within a flow with the assigned
	 * parameters.
	 * @param parameters the assigned action parameters
	 * @throws FlowArtifactException when no such action is found
	 */
	public Action getAction(FlowArtifactParameters parameters) throws FlowArtifactException;

	/**
	 * Retrieve the flow attribute mapper to be used in a subflow state with the
	 * provided id.
	 * @param id the id
	 * @return the attribute mapper
	 * @throws FlowArtifactException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException;

	/**
	 * Retrieve the transition criteria to drive state transitions with the
	 * provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws FlowArtifactException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException;

	/**
	 * Retrieve the view selector to make view selections in view states with
	 * the provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws FlowArtifactException when no such selector is found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactException;

	/**
	 * Retrieve the exception handler to handle state exceptions with the
	 * provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws FlowArtifactException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException;

	/**
	 * Retrieve the transition target state resolver with the specified id.
	 * @param id the id
	 * @return the target state resolver
	 * @throws FlowArtifactException when no such resolver is found
	 */
	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException;

	/**
	 * Create a new flow definition with the assigned parameters.
	 * @param parameters the assigned flow parameters
	 * @return the flow definition
	 */
	public Flow createFlow(FlowArtifactParameters parameters) throws FlowArtifactException;

	/**
	 * Create a new state definition with the assigned parameters.
	 * @param flow the state's owning flow
	 * @param stateType the state type
	 * @param parameters the assigned state parameters
	 * @return the state
	 */
	public State createState(Flow flow, Class stateType, FlowArtifactParameters parameters)
			throws FlowArtifactException;

	/**
	 * Create a new state transition with the assigned properties.
	 * @param attributes the assigned transition attributes
	 * @return the transition
	 */
	public Transition createTransition(UnmodifiableAttributeMap attributes) throws FlowArtifactException;

	/**
	 * Returns a generic bean (service) registry for accessing arbitrary beans.
	 * @return the generic service registry
	 * @throws UnsupportedOperationException when not supported by this factory
	 */
	public BeanFactory getServiceRegistry() throws UnsupportedOperationException;

	/**
	 * Returns a generic resource loader for accessing file-based resources.
	 * @return the generic resource loader
	 */
	public ResourceLoader getResourceLoader();

	/**
	 * Returns the expression parser for parsing expression strings.
	 * @return the expression parser
	 */
	public ExpressionParser getExpressionParser();
	
	/**
	 * Returns a generic type conversion service for converting between types,
	 * typically from string to a rich value object.
	 * @return the generic conversion service
	 */
	public ConversionService getConversionService();
}