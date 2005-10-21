package org.springframework.webflow.config.registry;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowAssembler;
import org.springframework.webflow.config.FlowBuilder;

/**
 * The default flow holder implementation that holds a flow built using a
 * configured flow builder.
 * @author Keith Donald
 */
public class FlowHolderImpl implements FlowHolder {

	/**
	 * The builder that used to build the held flow.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * The Flow held by this holder.
	 */
	private Flow flow;

	/**
	 * Creates a new static flow holder.
	 * @param flow the flow definition to hold.
	 */
	public FlowHolderImpl(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	public Flow getFlow() {
		synchronized (this) {
			if (flow == null) {
				refresh();
			}
		}
		return flow;
	}

	public void refresh() {
		flow = new FlowAssembler(flowBuilder).getFlow();
	}

	public String toString() {
		return new ToStringCreator(this).append("flowBuilder", flowBuilder).append("flow", getFlow()).toString();
	}
}