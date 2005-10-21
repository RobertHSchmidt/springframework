package org.springframework.webflow.config;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.Flow;

/**
 * A Flow definition registrar that registers flow definitions built by
 * configured Flow builders.
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
				Flow flow = new FlowFactoryBean(builder).getFlow();
				registry.registerFlowDefinition(flow);
			}
		}
		return registry;
	}

	public String toString() {
		return new ToStringCreator(this).append("flowBuilders", flowBuilders).toString();
	}
}