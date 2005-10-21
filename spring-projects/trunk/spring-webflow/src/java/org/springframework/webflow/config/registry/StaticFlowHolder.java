package org.springframework.webflow.config.registry;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Flow;

/**
 * A Flow holder that does nothing but hold a single static Flow.
 * @author Keith Donald
 */
public class StaticFlowHolder implements FlowHolder {

	/**
	 * The Flow held by this holder.
	 */
	private Flow flow;

	/**
	 * Creates a new static flow holder.
	 * @param flow the flow definition to hold.
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