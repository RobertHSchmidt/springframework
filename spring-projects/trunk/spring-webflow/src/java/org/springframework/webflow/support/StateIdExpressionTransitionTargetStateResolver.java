package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionTargetStateResolver;

public class StateIdExpressionTransitionTargetStateResolver implements TransitionTargetStateResolver {

	private static final String LAST_STATE_ALIAS = "lastState";
	
	private Expression stateIdExpression;
	
	public StateIdExpressionTransitionTargetStateResolver(Expression stateIdExpression) {
		this.stateIdExpression = stateIdExpression;
	}
	
	public State resolveTargetState(Transition transition, RequestContext context) {
		String stateId = (String)stateIdExpression.evaluateAgainst(context, getEvaluationContext(context));
		return transition.getSourceState().getFlow().getRequiredState(stateId);
	}

	protected Map getEvaluationContext(RequestContext context) {
		Map evalContext = new HashMap(1, 1);
		// ${#lastState == lastTransition.sourceState.id}
		if (context.getLastTransition() != null) {
			evalContext.put(LAST_STATE_ALIAS, context.getLastTransition().getSourceState().getId());
		}
		return evalContext;
	}
}
