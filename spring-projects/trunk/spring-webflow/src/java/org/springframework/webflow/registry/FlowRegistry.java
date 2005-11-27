package org.springframework.webflow.registry;

import org.springframework.webflow.execution.FlowLocator;

/**
 * A interface for registering Flow definitions. Extends the FlowRegistryMBean
 * management interface exposing monitoring and management operations. Extends
 * FlowLocator for accessing registered Flow definitions for execution at
 * runtime.
 * <p>
 * Is a <code>FlowLocator</code>, allowing for execution of flow definitions
 * managed in this registry.
 * 
 * @see org.springframework.webflow.execution.FlowLocator
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * 
 * @author Keith Donald
 */
public interface FlowRegistry extends FlowRegistryMBean, FlowLocator {

	/**
	 * Queries this registry to determine if a specific flow is contained within
	 * it.
	 * 
	 * @param id the flow id
	 * @return true if a flow is contained in this registry with the id provided
	 */
	public boolean containsFlow(String id);

	/**
	 * Register the flow definition in this registry. Registers a "holder", not
	 * the Flow definition itself. This allows the actual Flow definition to be
	 * loaded lazily only when needed, and also rebuilt at runtime when its
	 * underlying resource changes without redeploy.
	 * @param flowHolder a holder holding the flow definition to register
	 */
	public void registerFlow(FlowHolder flowHolder);

}