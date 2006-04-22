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
 * A support interface used by FlowBuilders at configuration time that serves
 * two roles:
 * <ol>
 * <li> As a service locator, to retrieve dependent (but externally managed flow
 * artifacts needed to build flow and state definitions. Such artifacts are
 * usually hosted in a backing registry, and may be shared by multiple flows.
 * <li> As an abstract factory, to create core flow-specific artifacts such as
 * {@link Flow}, {@link State}, {@link Transition}, and
 * {@link AbstractBeanInvokingAction bean invoking actions}. These artifacts
 * are unique to each flow and are typically not shared.
 * 
 * In general, implementations of this interface act as facades to accessing and
 * creating flow artifacts during {@link FlowAssembler flow assembly}. Finally,
 * this interface also exposes access to generic infrastructure services also
 * needed by flow assemblers.
 * 
 * @author Keith Donald
 */
public interface FlowArtifactFactory {

	/**
	 * Returns the Flow to be used as a subflow with the provided id.
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
	 * Returns the flow attribute mapper with the provided id. Flow attribute
	 * mappers are used from subflow states to map input and output attributes.
	 * @param id the id
	 * @return the attribute mapper
	 * @throws FlowArtifactException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException;

	/**
	 * Returns the transition criteria to drive state transitions with the
	 * provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws FlowArtifactException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException;

	/**
	 * Returns the view selector to make view selections in view states with the
	 * provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws FlowArtifactException when no such selector is found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactException;

	/**
	 * Returns the exception handler to handle state exceptions with the
	 * provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws FlowArtifactException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException;

	/**
	 * Returns the transition target state resolver with the specified id.
	 * @param id the id
	 * @return the target state resolver
	 * @throws FlowArtifactException when no such resolver is found
	 */
	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException;

	/**
	 * Factory method that creates a new {@link Flow} definition object.
	 * <p>
	 * Note this method does not return a fully configured Flow instance, it
	 * only encapsulates the selection of implementation. A
	 * {@link FlowAssembler} delegating to a calling {@link FlowBuilder} is
	 * expected to assemble the Flow fully before returning it to external
	 * clients.
	 * @param id the flow id the flow identifier, should be unique to all flows
	 * in an application (required)
	 * @param attributes attributes to assign to the Flow, which may also be
	 * used to affect flow construction; may be null
	 * @return the initial flow instance, ready for assembly by a FlowBuilder
	 * @throws FlowArtifactException an exception occured creating the Flow
	 * instance
	 */
	public Flow createFlow(String id, AttributeCollection attributes) throws FlowArtifactException;

	/**
	 * Factory method that creates a new view state, a state where a user is
	 * allowed to participate in the flow. This method is an atomic operation
	 * that returns a fully initialized state. It encapsulates the selection of
	 * the view state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param viewSelector the state view selector strategy; may be null
	 * @param transitions any transitions (paths) out of this state; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized view state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createViewState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException;

	/**
	 * Factory method that creates a new action state, a state where a system
	 * action is executed. This method is an atomic operation that returns a
	 * fully initialized state. It encapsulates the selection of the action
	 * state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param actions the actions to execute when the state is entered
	 * (required)
	 * @param transitions any transitions (paths) out of this state; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized action state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createActionState(String id, Flow flow, Action[] entryActions, Action[] actions,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException;

	/**
	 * Factory method that creates a new decision state, a state where a flow
	 * routing decision is made. This method is an atomic operation that returns
	 * a fully initialized state. It encapsulates the selection of the decision
	 * state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param transitions any transitions (paths) out of this state
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized decision state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createDecisionState(String id, Flow flow, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes)
			throws FlowArtifactException;

	/**
	 * Factory method that creates a new subflow state, a state where a parent
	 * flow spawns another flow as a subflow. This method is an atomic operation
	 * that returns a fully initialized state. It encapsulates the selection of
	 * the subflow state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param subflow the subflow definition (required)
	 * @param attributeMapper the subflow input and output attribute mapper; may
	 * be null
	 * @param transitions any transitions (paths) out of this state
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized subflow state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createSubflowState(String id, Flow flow, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) throws FlowArtifactException;

	/**
	 * Factory method that creates a new end state, a state where an executing
	 * flow session terminates. This method is an atomic operation that returns
	 * a fully initialized state. It encapsulates the selection of the end state
	 * implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param viewSelector the state confirmation view selector strategy; may be
	 * null
	 * @param outputMapper the state output mapper; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized subflow state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createEndState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes)
			throws FlowArtifactException;

	/**
	 * Factory method that creates a new transition, a path from one step in a
	 * flow to another. This method is an atomic operation that returns a fully
	 * initialized transition. It encapsulates the selection of the transition
	 * implementation as well as the transition assembly.
	 * @param matchingCriteria the criteria that matches the transition; may be
	 * null
	 * @param executionCriteria the criteria that governs execution of the
	 * transition after match; may be null
	 * @param targetStateResolver the resolver for calculating the target state
	 * of the transition (required)
	 * @param attributes attributes to assign to the transition, which may also
	 * be used to affect transition construction. May be null.
	 * @return the fully initialized transition instance
	 * @throws FlowArtifactException an exception occured creating the
	 * transition
	 */
	public Transition createTransition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria,
			TargetStateResolver targetStateResolver, AttributeCollection attributes) throws FlowArtifactException;

	/**
	 * Factory method that creates a bean invoking action, an adapter that
	 * adapts a method on an abitrary {@link Object} to the {@link Action}
	 * interface. This method is an atomic operation that returns a fully
	 * initialized Action. It encapsulates the selection of the action
	 * implementation as well as the action assembly.
	 * @param beanId the bean to be adapted to an Action instance
	 * @param methodSignature the method to invoke on the bean when the action
	 * is executed (required)
	 * @param resultSpecification the specification for what to do with the
	 * method return value; may be null
	 * @param attributes attributes that may be used to affect the bean invoking
	 * action's construction
	 * @return the fully configured bean invoking action instance
	 * @throws FlowArtifactException an exception occured creating the action
	 */
	public Action createBeanInvokingAction(String beanId, MethodSignature methodSignature,
			ResultSpecification resultSpecification, AttributeCollection attributes) throws FlowArtifactException;

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