/**
 * 
 */
package org.springframework.webflow.builder;

import org.springframework.webflow.execution.internal.builder.AbstractFlowBuilder;

public class SimpleFlowBuilder extends AbstractFlowBuilder {
	public void buildStates() {
		addEndState("end");
	}
}