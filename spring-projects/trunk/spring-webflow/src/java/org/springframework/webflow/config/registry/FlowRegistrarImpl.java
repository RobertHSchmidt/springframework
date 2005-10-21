package org.springframework.webflow.config.registry;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.config.FlowBuilder;

/**
 * A default flow registrar implementation that registers flow definitions built
 * by a configured set of Flow builders.
 * @author Keith Donald
 */
public class FlowRegistrarImpl implements FlowRegistrar {

	/**
	 * The builders that will build the Flow definitions to register.
	 */
	private FlowBuilder[] flowBuilders;

	/**
	 * Creates a Flow registrar with an initially empty builder list.
	 */
	public FlowRegistrarImpl() {

	}

	/**
	 * Creates a Flow registrar registering Flows built by the provided builder
	 * list.
	 */
	public FlowRegistrarImpl(FlowBuilder[] flowBuilders) {
		this.flowBuilders = flowBuilders;
	}

	/**
	 * Sets the builders that will build the Flow definitions to be registered.
	 */
	public void setFlowBuilders(FlowBuilder[] flowBuilders) {
		this.flowBuilders = flowBuilders;
	}

	public void registerFlowDefinitions(FlowRegistry registry) {
		if (flowBuilders != null) {
			for (int i = 0; i < flowBuilders.length; i++) {
				registry.registerFlowDefinition(new FlowHolderImpl(flowBuilders[i]));
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("flowBuilders", flowBuilders).toString();
	}
}