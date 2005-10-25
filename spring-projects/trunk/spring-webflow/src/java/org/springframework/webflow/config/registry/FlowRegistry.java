package org.springframework.webflow.config.registry;

import org.springframework.webflow.access.FlowLocator;

/**
 * A interface for registering Flow definitions. Extends the FlowRegistryMBean
 * management interface exposing monitoring and management operations. Extends
 * FlowLocator for accessing registered Flow definitions for execution at
 * runtime.
 * 
 * @author Keith Donald
 */
public interface FlowRegistry extends FlowRegistryMBean, FlowLocator {

	/**
	 * Register the flow definition in this registry. Registers a "holder", not
	 * the Flow itself. This allows the actual Flow definition to be loaded
	 * lazily only when needed, and rebuilt at runtime without redeploy.
	 * @param flowHolder a holder holding the flow definition to register
	 */
	public void registerFlowDefinition(FlowDefinitionHolder flowHolder);

}