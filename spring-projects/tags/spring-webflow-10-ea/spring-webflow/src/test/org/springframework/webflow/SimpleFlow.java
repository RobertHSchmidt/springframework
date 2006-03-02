/**
 * 
 */
package org.springframework.webflow;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.ExternalRedirectSelector;
import org.springframework.webflow.support.StaticTargetStateResolver;

public class SimpleFlow extends Flow {
	public SimpleFlow() {
		super("simpleFlow");

		ViewState state1 = new ViewState(this, "view");
		state1.setViewSelector(new ApplicationViewSelector("view"));
		state1.addTransition(new Transition(new StaticTargetStateResolver("end")));

		EndState state2 = new EndState(this, "end");
		state2.setViewSelector(new ExternalRedirectSelector(new StaticExpression("confirm")));
	}
}