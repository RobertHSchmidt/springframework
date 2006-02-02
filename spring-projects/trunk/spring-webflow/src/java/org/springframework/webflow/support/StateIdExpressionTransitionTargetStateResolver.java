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
package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionTargetStateResolver;

/**
 * Resolves the target state of a transition by using the String-value returned
 * from evaluating an {@link Expression expression} against the flow execution
 * {@link RequestContext request context}.
 * 
 * @author Keith Donald
 */
public class StateIdExpressionTransitionTargetStateResolver implements TransitionTargetStateResolver {

	/**
	 * Constant alias that points to the id of the source state of the last
	 * transition that occured in a web flow execution.
	 */
	private static final String LAST_STATE_ALIAS = "lastState";

	/**
	 * The expression to evaluate, expected to return a String used as the
	 * stateId.
	 */
	private Expression expression;

	/**
	 * Creates a new expression-based transition target state resolver.
	 * @param expression the expression
	 */
	public StateIdExpressionTransitionTargetStateResolver(Expression expression) {
		this.expression = expression;
	}

	public State resolveTargetState(Transition transition, RequestContext context) {
		String stateId = String.valueOf(expression.evaluateAgainst(context, getEvaluationContext(context)));
		return transition.getSourceState().getFlow().getRequiredState(stateId);
	}

	/**
	 * Setup a map with a few aliased values to make writing expression based
	 * transition target state resolution easier.
	 */
	protected Map getEvaluationContext(RequestContext context) {
		Map evalContext = new HashMap(1, 1);
		// ${#lastState == lastTransition.sourceState.id}
		if (context.getLastTransition() != null) {
			evalContext.put(LAST_STATE_ALIAS, context.getLastTransition().getSourceState().getId());
		}
		return evalContext;
	}
}
