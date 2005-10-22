package org.springframework.webflow.config.registry;

import org.springframework.webflow.access.FlowLocator;

/**
 * A interface for registering Flow definitions.
 * 
 * @author Keith Donald
 */
public interface FlowRegistry extends FlowRegistryMBean, FlowLocator {
	
	/**
	 * Register the flow definition in this registry.
	 * @param flowHolder a holder managing the flow definition to register
	 */
	public void registerFlowDefinition(FlowDefinitionHolder flowHolder);
	
}