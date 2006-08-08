/**
 * 
 */
package org.springframework.webflow;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.internal.support.ApplicationViewSelector;
import org.springframework.webflow.execution.internal.support.DefaultTargetStateResolver;
import org.springframework.webflow.execution.internal.support.ExternalRedirectSelector;

public class SimpleFlow extends Flow {
	public SimpleFlow() {
		super("simpleFlow");

		ViewState state1 = new ViewState(this, "view");
		state1.setViewSelector(new ApplicationViewSelector(new StaticExpression("view")));
		state1.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("end")));

		EndState state2 = new EndState(this, "end");
		state2.setViewSelector(new ExternalRedirectSelector(new StaticExpression("confirm")));
	}
}