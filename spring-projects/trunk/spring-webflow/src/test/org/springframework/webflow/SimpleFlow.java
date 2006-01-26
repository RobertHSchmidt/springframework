/**
 * 
 */
package org.springframework.webflow;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.support.RedirectViewSelector;
import org.springframework.webflow.support.SimpleViewSelector;
import org.springframework.webflow.support.StaticTransitionTargetStateResolver;

public class SimpleFlow extends Flow {
	public SimpleFlow() {
		super("simpleFlow");

		ViewState state1 = new ViewState(this, "view");
		state1.setViewSelector(new SimpleViewSelector("view"));
		state1.addTransition(new Transition(new StaticTransitionTargetStateResolver("end")));

		EndState state2 = new EndState(this, "end");
		state2.setViewSelector(new RedirectViewSelector(new StaticExpression("confirm")));
	}
}