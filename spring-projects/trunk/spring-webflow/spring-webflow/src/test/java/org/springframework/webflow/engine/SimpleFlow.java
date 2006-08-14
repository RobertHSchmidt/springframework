/**
 * 
 */
package org.springframework.webflow.engine;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.springframework.webflow.engine.support.ExternalRedirectSelector;

public class SimpleFlow extends Flow {
	public SimpleFlow() {
		super("simpleFlow");

		ViewState state1 = new ViewState(this, "view");
		state1.setViewSelector(new ApplicationViewSelector(new StaticExpression("view")));
		state1.getTransitionSet().add(new Transition("end"));

		EndState state2 = new EndState(this, "end");
		state2.setViewSelector(new ExternalRedirectSelector(new StaticExpression("confirm")));
	}
}