package org.springframework.webflow.config.registry;

/**
 * A JMX management interface for managing Flow definition registries at
 * runtime. Provides the ability to query the size and state of the registry, as
 * well as refresh registered Flow definitions at runtime.
 * 
 * @author Keith Donald
 */
public interface FlowRegistryMBean {

	/**
	 * Returns the names of the flow definitions registered in this registry.
	 * @return the flow definition names
	 */
	public String[] getFlowDefinitionIds();

	/**
	 * Return the number of flow definitions registered in this registry.
	 * @return the flow definition count;
	 */
	public int getFlowDefinitionCount();

	/**
	 * Refresh this flow definition registry, reloading all Flow definitions
	 * from their externalized representations.
	 */
	public void refresh();

	/**
	 * Refresh the Flow definition in this registry with the <code>id</code>
	 * provided, reloading it from it's externalized representation.
	 * @param id the id of the flow definition to refresh.
	 */
	public void refresh(String id);

}