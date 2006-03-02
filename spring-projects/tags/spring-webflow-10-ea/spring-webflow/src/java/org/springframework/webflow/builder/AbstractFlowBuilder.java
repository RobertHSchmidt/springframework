/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.builder;

import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.support.ActionTransitionCriteria;

/**
 * Base class for flow builders that programmatically build flows in Java
 * configuration code.
 * <p>
 * To give you an example of what a simple Java-based web flow builder
 * definition might look like, the following example defines the 'dynamic' web
 * flow roughly equivalent to the work flow statically implemented in Spring
 * MVC's simple form controller:
 * 
 * <pre>
 * public class CustomerDetailFlowBuilder extends AbstractFlowBuilder {
 *     public void buildStates() {
 *              
 *         // get customer information
 *         addActionState(&quot;getDetails&quot;, action(&quot;customerAction&quot;),
 *             on(success(), to(&quot;displayDetails&quot;)));
 *                      
 *         // view customer information               
 *         addViewState(&quot;displayDetails&quot;, &quot;customerDetails&quot;,
 *             on(submit(), to(&quot;bindAndValidate&quot;));
 *                  
 *         // bind and validate customer information updates 
 *         addActionState(&quot;bindAndValidate&quot;, action(&quot;customerAction&quot;),
 *             new Transition[] {
 *                 on(error(), to(&quot;displayDetails&quot;)),
 *                 on(success(), to(&quot;finish&quot;))
 *         });
 *                      
 *         // finish
 *         addEndState(&quot;finish&quot;);
 *     }
 * }
 * </pre>
 * 
 * What this Java-based FlowBuilder implementation does is add four states to a
 * flow. These include a "get" <code>ActionState</code> (the start state), a
 * <code>ViewState</code> state, a "bind and validate"
 * <code>ActionState</code>, and an end marker state (<code>EndState</code>).
 * 
 * The first state, an action state, will be assigned the indentifier
 * <code>getDetails</code>. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>The action instance with id <code>customerAction</code>. This is the
 * <code>Action</code> implementation that will execute when this state is
 * entered. In this example, that <code>Action</code> will go out to the DB,
 * load the Customer, and put it in the Flow's request context.
 * <li>A <code>success</code> transition to a default view state, called
 * <code>displayDetails</code>. This means when the <code>Action</code>
 * returns a <code>success</code> result event (aka outcome), the
 * <code>displayDetails</code> state will be entered.
 * <li>It will act as the start state for this flow (by default, the first
 * state added to a flow during the build process is treated as the start
 * state).
 * </ol>
 * 
 * The second state, a view state, will be identified as
 * <code>displayDetails</code>. This view state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>A view name called <code>customerDetails</code>. This is the logical
 * name of a view resource. This logical view name gets mapped to a physical
 * view resource (jsp, etc.) by the calling front controller (via a Spring view
 * resolver, or a Struts action forward, for example).
 * <li>A <code>submit</code> transition to a bind and validate action state,
 * indentified by the default id <code>bindAndValidate</code>. This means
 * when a <code>submit</code> event is signaled by the view (for example, on a
 * submit button click), the bindAndValidate action state will be entered and
 * the <code>bindAndValidate</code> method of the
 * <code>customerAction</code> <code>Action</code> implementation will be
 * executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as <code>
 * bindAndValidate</code>.
 * This action state will automatically be configured with the following
 * defaults:
 * <ol>
 * <li>An action bean named <code>customerAction</code> -- this is the name
 * of the <code>Action</code> implementation exported in the application
 * context that will execute when this state is entered. In this example, the
 * <code>Action</code> has a "bindAndValidate" method that will bind form
 * input in the HTTP request to a backing Customer form object, validate it, and
 * update the DB.
 * <li>A <code>success</code> transition to a default end state, called
 * <code>finish</code>. This means if the <code>Action</code> returns a
 * <code>success</code> result, the <code>finish</code> end state will be
 * transitioned to and the flow will terminate.
 * <li>An <code>error</code> transition back to the form view. This means if
 * the <code>Action</code> returns an <code>error</code> event, the <code>
 * displayDetails</code>
 * view state will be transitioned back to.
 * </ol>
 * 
 * The fourth and last state, an end state, will be indentified with the default
 * end state id <code>finish</code>. This end state is a marker that signals
 * the end of the flow. When entered, the flow session terminates, and if this
 * flow is acting as a root flow in the current flow execution, any
 * flow-allocated resources will be cleaned up. An end state can optionally be
 * configured with a logical view name to forward to when entered. It will also
 * trigger a state transition in a resuming parent flow if this flow was
 * participating as a spawned 'subflow' within a suspended parent flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowBuilder extends BaseFlowBuilder {

	/**
	 * Default constructor for subclassing.
	 */
	protected AbstractFlowBuilder() {
		super();
	}

	/**
	 * Create an instance of an abstract flow builder, using the specified
	 * factory to obtain needed flow services during configuation.
	 * @param flowArtifactFactory the artifact locator
	 */
	protected AbstractFlowBuilder(FlowArtifactFactory flowArtifactFactory) {
		super(flowArtifactFactory);
	}

	public void init(FlowArtifactParameters flowParameters) throws FlowBuilderException {
		setFlow(getFlowArtifactFactory().createFlow(flowParameters.putAll(flowAttributes())));
	}

	protected MappingBuilder mapping() {
		MappingBuilder mapping = new MappingBuilder(getFlowArtifactFactory().getExpressionParser());
		mapping.setConversionService(getFlowArtifactFactory().getConversionService());
		return mapping;
	}
	
	/**
	 * Hook subclasses may override to provide additional properties for the
	 * flow built by this builder. Returns <code>null</code> by default.
	 * @return additional properties describing the flow being built
	 */
	protected AttributeCollection flowAttributes() {
		return null;
	}

	public abstract void buildStates();

	public void buildExceptionHandlers() {
		// default implementation assumes no exception handlers to build
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A marker has a <code>null</code> <code>viewName</code> and assumes
	 * the HTTP response has already been written when entered. The marker notes
	 * that control should be returned to the HTTP client.
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param transition a single supported transition for this state, mapping a
	 * path from this state to another state (triggered by an event)
	 * @return the view marker state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, Transition transition) throws IllegalArgumentException {
		return addViewState(stateId, (String)null, transition);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A view marker has a <code>null</code> <code>viewName</code> and
	 * assumes the HTTP response has already been written when entered. The
	 * marker notes that control should be returned to the HTTP client.
	 * <p>
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @return the view marker state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, Transition[] transitions) throws IllegalArgumentException {
		return addViewState(stateId, (String)null, transitions);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A view marker has a <code>null</code> <code>viewName</code> and
	 * assumes the HTTP response has already been written when entered. The
	 * marker notes that control should be returned to the HTTP client.
	 * <p>
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @param attributes additional attributes describing the state
	 * @return the view marker state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, Transition[] transitions, AttributeCollection attributes)
			throws IllegalArgumentException {
		return addViewState(stateId, (String)null, transitions, attributes);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * @param stateId the <code>ViewState</code> id; must be locally unique to
	 * the flow built by this builder
	 * @param viewName the name of the logical view to render; this name will be
	 * mapped to a physical resource template such as a JSP when the ViewState
	 * is entered and control returns to the front controller
	 * @param transition a single supported transition for this state, mapping a
	 * path from this state to another state (triggered by an event)
	 * @return the view state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, String viewName, Transition transition)
			throws IllegalArgumentException {
		return addViewState(stateId, viewSelector(viewName), new Transition[] { transition }, null);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param viewName the name of the logical view to render; this name will be
	 * mapped to a physical resource template such as a JSP when the ViewState
	 * is entered and control returns to the front controller
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @return the view state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, String viewName, Transition[] transitions)
			throws IllegalArgumentException {
		return addViewState(stateId, viewSelector(viewName), transitions, null);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param viewName the name of the logical view to render; this name will be
	 * mapped to a physical resource template such as a JSP when the ViewState
	 * is entered and control returns to the front controller
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @param attributes additional attributes describing the state
	 * @return the view state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, String viewName, Transition[] transitions,
			AttributeCollection attributes) throws IllegalArgumentException {
		return addViewState(stateId, viewSelector(viewName), transitions, attributes);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param viewSelector the factory to produce a selection noting the name of
	 * the logical view to render; this name will be mapped to a physical
	 * resource template such as a JSP when the ViewState is entered and control
	 * returns to the front controller
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @return the view state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, ViewSelector viewSelector, Transition[] transitions)
			throws IllegalArgumentException {
		return addViewState(stateId, viewSelector, transitions, null);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * @param stateId the <code>ViewState</code> id; must be unique in the
	 * context of the flow built by this builder
	 * @param viewSelector the factory to produce a selection noting the name of
	 * the logical view to render; this name will be mapped to a physical
	 * resource template such as a JSP when the ViewState is entered and control
	 * returns to the front controller
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @param attributes additional attributes describing the state
	 * @return the view state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ViewState addViewState(String stateId, ViewSelector viewSelector, Transition[] transitions,
			AttributeCollection attributes) throws IllegalArgumentException {
		ViewState state = new ViewState(getFlow(), stateId);
		state.setViewSelector(viewSelector);
		state.getTransitionSet().addAll(transitions);
		state.getAttributeMap().putAll(attributes);
		return state;
	}

	/**
	 * Turn given view name into a corresponding view selector.
	 * @param viewName the view name (might be encoded)
	 * @return the corresponding view selector
	 */
	protected ViewSelector viewSelector(String viewName) {
		return (ViewSelector)fromStringTo(ViewSelector.class).execute(viewName);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId the qualified stateId for the state; must be unique in the
	 * context of the flow built by this builder
	 * @param action the action implementation
	 * @param transition a single supported transition for this state, mapping a
	 * path from this state to another state (triggered by an event)
	 * @return the action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action action, Transition transition)
			throws IllegalArgumentException {
		return addActionState(stateId, new Action[] { action }, new Transition[] { transition }, null);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId the qualified stateId for the state; must be unique in the
	 * context of the flow built by this builder
	 * @param action the action implementation
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @return the action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action action, Transition[] transitions)
			throws IllegalArgumentException {
		return addActionState(stateId, new Action[] { action }, transitions, null);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId the qualified stateId for the state; must be unique in the
	 * context of the flow built by this builder
	 * @param action the action implementation
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @param attributes additional attributes describing the state
	 * @return the action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action action, Transition[] transitions,
			AttributeCollection attributes) throws IllegalArgumentException {
		return addActionState(stateId, new Action[] { action }, transitions, attributes);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes one or more <code>Action</code> implementations
	 * when entered.
	 * @param stateId the qualified stateId for the state; must be unique in the
	 * context of the flow built by this builder
	 * @param actions the action implementations, to be executed in order until
	 * a valid transitional result is returned (Chain of Responsibility)
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @return the action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action[] actions, Transition[] transitions)
			throws IllegalArgumentException {
		return addActionState(stateId, actions, transitions, null);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes one or more <code>Action</code> implementations
	 * when entered.
	 * @param stateId the qualified stateId for the state; must be unique in the
	 * context of the flow built by this builder
	 * @param actions the action implementations, to be executed in order until
	 * a valid transitional result is returned (Chain of Responsibility)
	 * @param transitions the supported transitions for this state, where each
	 * transition maps a path from this state to another state (triggered by an
	 * event)
	 * @param attributes additional attributes describing the state
	 * @return the action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action[] actions, Transition[] transitions,
			AttributeCollection attributes) throws IllegalArgumentException {
		ActionState state = new ActionState(getFlow(), stateId);
		state.getTransitionSet().addAll(transitions);
		state.getActionList().addAll(actions);
		state.getAttributeMap().putAll(attributes);
		return state;
	}

	/**
	 * Request that the action with the specified id be executed when the action
	 * state being built is entered. Simply looks the action up by name and
	 * returns it.
	 * @param id the action id
	 * @return the action
	 * @throws FlowArtifactException the action could not be resolved
	 */
	protected Action action(String id) throws FlowArtifactException {
		return getFlowArtifactFactory().getAction(new FlowArtifactParameters(id));
	}

	/**
	 * Creates an annotated action with a single property that indicates which
	 * method should be invoked on the target action when the state is entered.
	 * @param encodedMethodKey the method name to invoke on the action</code>
	 * @return the annotated action
	 */
	protected AnnotatedAction method(String encodedMethodKey, Action action) {
		MethodSignature method = (MethodSignature)fromStringTo(MethodSignature.class).execute(encodedMethodKey);
		AnnotatedAction annotatedAction = new AnnotatedAction(action);
		annotatedAction.setMethod(method);
		return annotatedAction;
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id, must be unique among all states of the flow
	 * built by this builder
	 * @param subFlow the flow to be used as a subflow
	 * @param transition the single supported transition out of the state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, Transition transition) {
		return addSubflowState(stateId, subFlow, null, new Transition[] { transition }, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id, must be unique among all states of the flow
	 * built by this builder
	 * @param subFlow the flow to be used as a subflow
	 * @param transitions the eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, Transition[] transitions) {
		return addSubflowState(stateId, subFlow, null, transitions, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id, must be unique among all states of the flow
	 * built by this builder
	 * @param subFlow the flow to be used as a subflow
	 * @param transitions the eligible set of state transitions
	 * @param attributes additional attributes describing the state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, Transition[] transitions,
			AttributeCollection attributes) {
		return addSubflowState(stateId, subFlow, null, transitions, attributes);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id, must be unique among all states of the flow
	 * built by this builder
	 * @param subFlow the flow to be used as a subflow
	 * @param attributeMapper the attribute mapper to map attributes between the
	 * flow built by this builder and the subflow
	 * @param transition the single supported transition out of the state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, FlowAttributeMapper attributeMapper,
			Transition transition) {
		return addSubflowState(stateId, subFlow, attributeMapper, new Transition[] { transition }, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id
	 * @param subFlow the flow definition to be used as the subflow
	 * @param attributeMapper the attribute mapper to map attributes between the
	 * flow built by this builder and the subflow
	 * @param transitions the eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, FlowAttributeMapper attributeMapper,
			Transition[] transitions) {
		return addSubflowState(stateId, subFlow, attributeMapper, transitions, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * id.
	 * @param stateId the state id
	 * @param subFlow the flow definition to be used as the subflow
	 * @param attributeMapper the attribute mapper to map attributes between the
	 * flow built by this builder and the subflow
	 * @param transitions the eligible set of state transitions
	 * @param attributes additional attributes describing the state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected SubflowState addSubflowState(String stateId, Flow subFlow, FlowAttributeMapper attributeMapper,
			Transition[] transitions, AttributeCollection attributes) {
		SubflowState state = new SubflowState(getFlow(), stateId, subFlow);
		state.getTransitionSet().addAll(transitions);
		state.setAttributeMapper(attributeMapper);
		state.getAttributeMap().putAll(attributes);
		return state;
	}

	/**
	 * Request that the attribute mapper with the specified name be used to map
	 * attributes between a parent flow and a spawning subflow when the subflow
	 * state being constructed is entered.
	 * @param id the id of the attribute mapper that will map attributes between
	 * the flow built by this builder and the subflow
	 * @return the attribute mapper
	 * @throws FlowArtifactException no FlowAttributeMapper implementation was
	 * exported with the specified id
	 */
	protected FlowAttributeMapper attributeMapper(String id) throws FlowArtifactException {
		return getFlowArtifactFactory().getAttributeMapper(id);
	}

	/**
	 * Request that the <code>Flow</code> with the specified flowId be spawned
	 * as a subflow when the subflow state being built is entered. Simply
	 * resolves the subflow definition by id and returns it; throwing a
	 * fail-fast exception if it does not exist.
	 * @param id the flow definition id
	 * @return the flow to be used as a subflow, this should be passed to a
	 * addSubflowState call
	 * @throws FlowArtifactException when the flow cannot be resolved
	 */
	protected Flow flow(String id) throws FlowArtifactException {
		return getFlowArtifactFactory().getSubflow(id);
	}

	/**
	 * Adds a decision state to the flow built by this builder with the
	 * specified id.
	 * @param stateId the state id
	 * @param matchingCriteria the criteria that determines what boolean
	 * true/fase decision to make
	 * @param ifTrueStateId the state to transition to if the criteria is true
	 * @param ifFalseStateId the state to transition if the criteria is false
	 * @return the configured decision state
	 * @throws IllegalArgumentException
	 */
	protected DecisionState addDecisionState(String stateId, TransitionCriteria matchingCriteria, String ifTrueStateId,
			String ifFalseStateId) throws IllegalArgumentException {
		return addDecisionState(stateId, matchingCriteria, ifTrueStateId, ifFalseStateId, null);
	}

	/**
	 * Adds a decision state to the flow built by this builder with the
	 * specified id.
	 * @param stateId the state id
	 * @param matchingCriteria the criteria that determines what boolean
	 * true/fase decision to make
	 * @param ifTrueStateId the state to transition to if the criteria is true
	 * @param ifFalseStateId the state to transition if the criteria is false
	 * @param attributes custom decision state attributes
	 * @return the configured decision state
	 * @throws IllegalArgumentException
	 */
	protected DecisionState addDecisionState(String stateId, TransitionCriteria matchingCriteria, String ifTrueStateId,
			String ifFalseStateId, AttributeCollection attributes) throws IllegalArgumentException {
		Transition[] transitions = new Transition[2];
		transitions[0] = new Transition(to(ifTrueStateId));
		transitions[0].setMatchingCriteria(matchingCriteria);
		transitions[1] = new Transition(to(ifFalseStateId));
		return addDecisionState(stateId, transitions, attributes);
	}

	/**
	 * Adds a decision state to the flow built by this builder with the
	 * specified id.
	 * @param stateId the state id
	 * @param transitions the state's supported transitions, evaluated in the
	 * specified order until a match is found
	 * @return the configured decision state
	 * @throws IllegalArgumentException
	 */
	protected DecisionState addDecisionState(String stateId, Transition[] transitions) throws IllegalArgumentException {
		return addDecisionState(stateId, transitions, null);
	}

	/**
	 * Adds a decision state to the flow built by this builder with the
	 * specified id.
	 * @param stateId the state id
	 * @param transitions the state's supported transitions, evaluated in the
	 * specified order until a match is found
	 * @param attributes custom decision state attributes
	 * @return the configured decision state
	 * @throws IllegalArgumentException
	 */
	protected DecisionState addDecisionState(String stateId, Transition[] transitions, AttributeCollection attributes)
			throws IllegalArgumentException {
		DecisionState state = new DecisionState(getFlow(), stateId);
		state.getTransitionSet().addAll(transitions);
		state.getAttributeMap().putAll(attributes);
		return state;
	}

	/**
	 * Adds an end state with the specified id. The created end state will be a
	 * marker end state with no associated view.
	 * @param stateId the end state id
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId) throws IllegalArgumentException {
		return addEndState(stateId, (String)null);
	}

	/**
	 * Adds an end state with the specified id that will display the specified
	 * view when entered as part of a terminating flow execution.
	 * @param stateId the end state id
	 * @param viewName the view name
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, String viewName) throws IllegalArgumentException {
		return addEndState(stateId, viewSelector(viewName), null, null);
	}

	/**
	 * Adds an end state with the specified id exposing the specified attributes
	 * as output. The created end state will be a marker end state with no
	 * associated view.
	 * @param stateId the end state id
	 * @param outputAttributeNames the names of the attributes in flow scope to
	 * expose as output attributes, making them available for output mapping by
	 * a calling parent flow
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, String[] outputAttributeNames) throws IllegalArgumentException {
		return addEndState(stateId, viewSelector(null), outputAttributeNames, null);
	}

	/**
	 * Adds an end state with the specified id that will display the specified
	 * view when entered as part of a terminating flow execution.
	 * @param stateId the end state id
	 * @param viewName the view name
	 * @param outputAttributeNames the names of the attributes in flow scope to
	 * expose as output attributes, making them available for output mapping by
	 * a calling parent flow
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, String viewName, String[] outputAttributeNames)
			throws IllegalArgumentException {
		return addEndState(stateId, viewSelector(viewName), outputAttributeNames, null);
	}

	/**
	 * Adds an end state with the specified id that will message the specified
	 * view selector to produce a view to display when entered as part of a root
	 * flow termination.
	 * @param stateId the end state id
	 * @param viewSelector the view selector
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, ViewSelector viewSelector) throws IllegalArgumentException {
		return addEndState(stateId, viewSelector, null, null);
	}

	/**
	 * Adds an end state with the specified id that will display the specified
	 * view when entered as part of a terminating flow execution.
	 * @param stateId the end state id
	 * @param viewSelector the view selector
	 * @param outputAttributeNames the names of the attributes in flow scope to
	 * expose as output attributes, making them available for output mapping by
	 * a calling parent flow
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, ViewSelector viewSelector, String[] outputAttributeNames)
			throws IllegalArgumentException {
		return addEndState(stateId, viewSelector, outputAttributeNames, null);
	}

	/**
	 * Adds an end state with the specified id that will message the specified
	 * view selector to produce a view to display when entered as part of a root
	 * flow termination.
	 * @param stateId the end state id
	 * @param viewSelector the view selector
	 * @param outputAttributeNames the names of the attributes in flow scope to
	 * expose as output attributes, making them available for output mapping by
	 * a calling parent flow
	 * @param attributes additional attributes describing the state
	 * @return the end state
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected EndState addEndState(String stateId, ViewSelector viewSelector, String[] outputAttributeNames,
			AttributeCollection attributes) throws IllegalArgumentException {
		EndState state = new EndState(getFlow(), stateId);
		state.setViewSelector(viewSelector);
		state.addOutputAttributeNames(outputAttributeNames);
		state.getAttributeMap().putAll(attributes);
		return state;
	}

	/**
	 * Creates a transition criteria that is used to match a Transition. The
	 * criteria is based on the provided expression string.
	 * @param transitionCriteriaExpression the transition criteria expression,
	 * typically simply a static event identifier (e.g. "submit")
	 * @return the transition criteria
	 */
	protected TransitionCriteria on(String transitionCriteriaExpression) {
		return (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(transitionCriteriaExpression);
	}

	/**
	 * Creates a transition target state resolver that is used to take a
	 * Transition to its target state during execution.
	 * @param targetStateExpression the target state expression, typically
	 * simply the targetStateId
	 * @return the target state resolver
	 */
	protected TargetStateResolver to(String targetStateExpression) {
		return (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(targetStateExpression);
	}

	/**
	 * Creates a transition stating:
	 * <tt>On the occurrence of an event that matches the criteria defined by
	 * ${criteria}, transition to the state resolved by ${targetStateResolver}.
	 * </tt>
	 * @param criteria the transition criteria
	 * @param targetStateResolver the resolver of the target state of the
	 * transition
	 * @return the transition (event matching criteria->stateId)
	 */
	protected Transition transition(TransitionCriteria criteria, TargetStateResolver targetStateResolver) {
		Transition transition = new Transition(targetStateResolver);
		transition.setMatchingCriteria(criteria);
		return transition;
	}

	/**
	 * Creates a transition stating:
	 * <tt>On the occurrence of an event that matches the criteria defined by
	 * ${criteria}, transition to the state resolved by ${targetStateResolver} if the ${executionCriteria}
	 * holds true.
	 * </tt>
	 * @param criteria the transition criteria
	 * @param targetStateResolver the resolver of the target state of the
	 * transition
	 * @param executionCriteria criteria that decide whether or not the
	 * transition can complete execution
	 * @return the transition (event matching criteria->stateId)
	 */
	protected Transition transition(TransitionCriteria criteria, TargetStateResolver targetStateResolver,
			TransitionCriteria executionCriteria) {
		Transition transition = transition(criteria, targetStateResolver);
		transition.setExecutionCriteria(executionCriteria);
		return transition;
	}

	/**
	 * Creates a <code>TransitionCriteria</code> that will execute the
	 * specified action when the Transition is executed but before the
	 * transition's target state is entered.
	 * <p>
	 * This criteria will only allow the Transition to complete execution if the
	 * Action completes successfully.
	 * @param action the action to execute after a transition is matched but
	 * before it transitions to its target state
	 * @return the transition execution criteria
	 */
	protected TransitionCriteria ifSuccessful(Action action) {
		return new ActionTransitionCriteria(action);
	}

	/**
	 * Creates the <code>success</code> event id. "Success" indicates that an
	 * action completed successfuly.
	 * @return the event id
	 */
	protected String success() {
		return "success";
	}

	/**
	 * Creates the <code>error</code> event id. "Error" indicates that an
	 * action completed with an error status.
	 * @return the event id
	 */
	protected String error() {
		return "error";
	}

	/**
	 * Creates the <code>submit</code> event id. "Submit" indicates the user
	 * submitted a request (form) for processing.
	 * @return the event id
	 */
	protected String submit() {
		return "submit";
	}

	/**
	 * Creates the <code>back</code> event id. "Back" indicates the user wants
	 * to go to the previous step in the flow.
	 * @return the event id
	 */
	protected String back() {
		return "back";
	}

	/**
	 * Creates the <code>cancel</code> event id. "Cancel" indicates the flow
	 * was aborted because the user changed their mind.
	 * @return the event id
	 */
	protected String cancel() {
		return "cancel";
	}

	/**
	 * Creates the <code>finish</code> event id. "Finish" indicates the flow
	 * has finished processing.
	 * @return the event id
	 */
	protected String finish() {
		return "finish";
	}

	/**
	 * Creates the <code>select</code> event id. "Select" indicates an object
	 * was selected for processing or display.
	 * @return the event id
	 */
	protected String select() {
		return "select";
	}

	/**
	 * Creates the <code>edit</code> event id. "Edit" indicates an object was
	 * selected for creation or updating.
	 * @return the event id
	 */
	protected String edit() {
		return "edit";
	}

	/**
	 * Creates the <code>add</code> event id. "Add" indicates a child object
	 * is being added to a parent collection.
	 * @return the event id
	 */
	protected String add() {
		return "add";
	}

	/**
	 * Creates the <code>delete</code> event id. "Delete" indicates a object
	 * is being removed.
	 * @return the event id
	 */
	protected String delete() {
		return "delete";
	}

	/**
	 * Creates the <code>yes</code> event id. "Yes" indicates a true result
	 * was returned.
	 * @return the event id
	 */
	protected String yes() {
		return "yes";
	}

	/**
	 * Creates the <code>no</code> event id. "False" indicates a false result
	 * was returned.
	 * @return the event id
	 */
	protected String no() {
		return "no";
	}
}