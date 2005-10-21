package org.springframework.webflow.config;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Flow;

/**
 * A Flow definition registrar that registers all flow definitions built by a
 * configured set of Flow builders.
 * @author Keith Donald
 */
public class FlowRegistrarImpl implements FlowRegistrar {

	/**
	 * The builders that will build the Flow definitions to register.
	 */
	private FlowBuilder[] flowBuilders;

	/**
	 * Sets the flow builders that will build the Flow definitions to register.
	 */
	public void setFlowBuilders(FlowBuilder[] flowBuilders) {
		this.flowBuilders = flowBuilders;
	}

	public ConfigurableFlowRegistry registerFlowDefinitions(ConfigurableFlowRegistry registry) {
		if (flowBuilders != null) {
			for (int i = 0; i < flowBuilders.length; i++) {
				FlowBuilder builder = flowBuilders[i];
				Flow flow = new FlowAssembler(builder).getFlow();
				registry.registerFlowDefinition(flow);
			}
		}
		return registry;
	}

	public String toString() {
		return new ToStringCreator(this).append("flowBuilders", flowBuilders).toString();
	}
}