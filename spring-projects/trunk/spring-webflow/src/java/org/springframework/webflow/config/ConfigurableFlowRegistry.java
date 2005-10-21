package org.springframework.webflow.config;

import org.springframework.webflow.Flow;

/**
 * A subinterface of FlowRegistry that adds mutable operations for registering
 * new flow definitions.
 * @author Keith Donald
 */
public interface ConfigurableFlowRegistry extends FlowRegistry {

	/**
	 * Register the flow definition in this registry.
	 * @param flow The flow to register
	 */
	public void registerFlowDefinition(Flow flow);

	/**
	 * Register the flow definition in this registry.
	 * @param flowHolder a holder for the flow to register
	 */
	public void registerFlowDefinition(FlowHolder flowHolder);

}