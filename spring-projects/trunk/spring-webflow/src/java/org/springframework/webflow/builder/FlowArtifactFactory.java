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

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;

public class FlowArtifactFactory {
	
	public Flow createFlow(String id, AttributeCollection attributes) throws FlowArtifactException {
		Flow flow = new Flow(id);
		flow.getAttributeMap().putAll(attributes);
		return flow;
	}

	public State createViewState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ViewState viewState = new ViewState(flow, id);
		if (viewSelector != null) {
			viewState.setViewSelector(viewSelector);
		}
		configureCommonProperties(viewState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return viewState;
	}

	public State createActionState(String id, Flow flow, Action[] entryActions, Action[] actions,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ActionState actionState = new ActionState(flow, id);
		actionState.getActionList().addAll(actions);
		configureCommonProperties(actionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return actionState;
	}

	public State createDecisionState(String id, Flow flow, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes)
			throws FlowArtifactException {
		DecisionState decisionState = new DecisionState(flow, id);
		configureCommonProperties(decisionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return decisionState;
	}

	public State createSubflowState(String id, Flow flow, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) throws FlowArtifactException {
		SubflowState subflowState = new SubflowState(flow, id, subflow);
		if (attributeMapper != null) {
			subflowState.setAttributeMapper(attributeMapper);
		}
		configureCommonProperties(subflowState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return subflowState;
	}

	public State createEndState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes)
			throws FlowArtifactException {
		EndState endState = new EndState(flow, id);
		if (viewSelector != null) {
			endState.setViewSelector(viewSelector);
		}
		if (outputMapper != null) {
			endState.setOutputMapper(outputMapper);
		}
		configureCommonProperties(endState, entryActions, exceptionHandlers, attributes);
		return endState;
	}

	public Transition createTransition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria,
			TargetStateResolver targetStateResolver, AttributeCollection attributes) throws FlowArtifactException {
		Transition transition = new Transition(targetStateResolver);
		if (matchingCriteria != null) {
			transition.setMatchingCriteria(matchingCriteria);
		}
		if (executionCriteria != null) {
			transition.setExecutionCriteria(executionCriteria);
		}
		transition.getAttributeMap().putAll(attributes);
		return transition;
	}
	
	private void configureCommonProperties(TransitionableState state, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes) {
		configureCommonProperties(state, entryActions, exceptionHandlers, attributes);
		state.getTransitionSet().addAll(transitions);
		state.getExitActionList().addAll(exitActions);
	}

	private void configureCommonProperties(State state, Action[] entryActions,
			StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes) {
		state.getEntryActionList().addAll(entryActions);
		state.getExceptionHandlerSet().addAll(exceptionHandlers);
		state.getAttributeMap().putAll(attributes);
	}
}