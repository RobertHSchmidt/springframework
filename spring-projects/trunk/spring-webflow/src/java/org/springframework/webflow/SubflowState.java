/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow;

import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transitionable state that spawns a subflow when executed. When the subflow
 * this state spawns ends, the ending result is used as grounds for a state
 * transition out of this state.
 * <p>
 * A subflow state may be configured to map input data from its flow -- acting
 * as the parent flow -- down to the subflow when the subflow is spawned. In
 * addition, output data produced by the subflow may be mapped up to the parent
 * flow when the subflow ends and the parent flow resumes. See the
 * {@link FlowAttributeMapper} interface definition for more information on how
 * to do this. The logic for ending a subflow is located in the {@link EndState}
 * implementation.
 * 
 * @see org.springframework.webflow.FlowAttributeMapper
 * @see org.springframework.webflow.EndState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SubflowState extends TransitionableState implements FlowAttributeMapper {

	/**
	 * Name of the property used to indicate the start state in which to start
	 * the subflow.
	 */
	public static final String START_STATE_PROPERTY = "startState";

	/**
	 * The subflow that should be spawned when this subflow state is entered.
	 */
	private Flow subflow;

	/**
	 * The attribute mapper that should map attributes from the parent flow down
	 * to the spawned subflow and visa versa.
	 */
	private FlowAttributeMapper attributeMapper;
	
	/**
	 * Default constructor for bean style usage.
	 * @see TransitionableState#TransitionableState()
	 * @see #setSubflow(Flow)
	 * @see #setAttributeMapper(FlowAttributeMapper)
	 */
	public SubflowState() {
	}

	/**
	 * Create a new subflow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subflow the subflow to spawn
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public SubflowState(Flow flow, String id, Flow subflow, Transition[] transitions) throws IllegalArgumentException {
		this(flow, id, subflow, null, transitions);
	}

	/**
	 * Create a new subflow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subflow the subflow to spawn
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public SubflowState(Flow flow, String id, Flow subflow, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		this(flow, id, subflow, null, transitions, properties);
	}

	/**
	 * Create a new subflow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subflow the subflow to spawn
	 * @param attributeMapper the attribute mapper to use
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public SubflowState(Flow flow, String id, Flow subflow, FlowAttributeMapper attributeMapper,
			Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setSubflow(subflow);
		setAttributeMapper(attributeMapper);
	}

	/**
	 * Create a new subflow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subflow the subflow to spawn
	 * @param attributeMapper the attribute mapper to use
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public SubflowState(Flow flow, String id, Flow subflow, FlowAttributeMapper attributeMapper,
			Transition[] transitions, Map properties) throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		setSubflow(subflow);
		setAttributeMapper(attributeMapper);
	}

	/**
	 * Set the subflow that will be spawned by this state.
	 * @param subflow the subflow to spawn
	 */
	public void setSubflow(Flow subflow) {
		Assert.notNull(subflow, "A subflow state must have a subflow");
		this.subflow = subflow;
	}

	/**
	 * Returns the subflow spawned by this state.
	 */
	public Flow getSubflow() {
		return this.subflow;
	}

	/**
	 * Set the attribute mapper to use to map model data between parent and child
	 * subflow model. Can be null if no mapping is needed.
	 */
	public void setAttributeMapper(FlowAttributeMapper attributeMapper) {
		this.attributeMapper = attributeMapper;
	}

	/**
	 * Returns the attribute mapper used to map data between parent and child subflow
	 * model, or null if no mapping is needed.
	 */
	public FlowAttributeMapper getAttributeMapper() {
		return this.attributeMapper;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * Entering this state, creates the subflow input map and spawns the subflow
	 * in the current flow execution.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state execution
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning subflow '" + getSubflow().getId() + "' within flow '" + getFlow().getId() + "'");
		}
		return context.start(getSubflow(), getSubflowStartState(context), createSubflowInput(context));
	}

	public Map createSubflowInput(RequestContext context) {
		if (getAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attribute mapper to map attributes "
						+ "down to the spawned subflow for access within the subflow");
			}
			return this.attributeMapper.createSubflowInput(context);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attribute mapper configured for this subflow state '" + getId()
						+ "' -- as a result, no attributes in flow scope will be passed to the spawned subflow '"
						+ subflow.getId() + "'");
			}
			return null;
		}
	}

	public void mapSubflowOutput(RequestContext context) {
		if (getAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attribute mapper to map subflow attributes back up to the "
						+ "resuming flow -- It will have access to attributes passed up by the completed subflow");
			}
			this.attributeMapper.mapSubflowOutput(context);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attribute mapper is configured for the resuming state '"
						+ getId()
						+ "' -- as a result, no attributes in the ending subflow scope will be passed to the resuming flow");
			}
		}
	}

	/**
	 * Returns the start state to use in the subflow, or null if the default
	 * start state configured for the flow should be used.
	 */
	protected State getSubflowStartState(RequestContext context) {
		if (containsProperty(START_STATE_PROPERTY)) {
			return getFlow().getRequiredState((String)getProperty(START_STATE_PROPERTY));
		}
		else {
			// default start state of flow will be used
			return null;
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("subflow", subflow.getId()).append("attributeMapper", attributeMapper);
		super.createToString(creator);
	}
}