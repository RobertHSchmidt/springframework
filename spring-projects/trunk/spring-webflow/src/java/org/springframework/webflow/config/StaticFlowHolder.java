package org.springframework.webflow.config;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Flow;

/**
 * @author Keith Donald
 */
public class StaticFlowHolder implements FlowHolder {

	/**
	 * 
	 */
	private Flow flow;

	/**
	 * @param flow
	 */
	public StaticFlowHolder(Flow flow) {
		this.flow = flow;
	}

	public Flow getFlow() {
		return flow;
	}

	public String toString() {
		return new ToStringCreator(this).append("flow", getFlow()).toString();
	}
}