package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.method.MethodSignature;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.action.ResultSpecification;

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
	public Action getAction(String id) throws FlowArtifactException;

	/**
	 * Returns true if the action with the given <code>actionId</code> is an
	 * actual implementation of the {@link Action} interface. It could be an
	 * arbitrary bean (any <code>java.lang.Object</code>), at which it needs
	 * to be adapted by a
	 * {@link AbstractBeanInvokingAction bean invoking action}.
	 * @param actionId the action id
	 * @return true if the action is an Action, false otherwise
	 * @throws FlowArtifactException when no such action is found
	 */
	public boolean isAction(String actionId) throws FlowArtifactException;

	/**
	 * Returns true if the action with the given <code>actionId</code> is a
	 * {@link MultiAction} instance.
	 * @param actionId the action id
	 * @return true if the action is a multi action, false otherwise
	 * @throws FlowArtifactException when no such action is found
	 */
	public boolean isMultiAction(String actionId) throws FlowArtifactException;

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

	public Flow createFlow(String id, AttributeCollection attributes) throws FlowArtifactException;

	public State createViewState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException;

	public State createActionState(String id, Flow flow, Action[] entryActions, Action[] actions,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException;

	public State createDecisionState(String id, Flow flow, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes)
			throws FlowArtifactException;

	public State createSubflowState(String id, Flow flow, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) throws FlowArtifactException;

	public State createEndState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes)
			throws FlowArtifactException;

	public Transition createTransition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria,
			TargetStateResolver targetStateResolver, AttributeCollection attributes) throws FlowArtifactException;

	public Action createBeanInvokingAction(String beanId, MethodSignature methodSignature,
			ResultSpecification resultSpecification, AttributeCollection attributes);

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