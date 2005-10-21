package org.springframework.webflow.config.registry;

/**
 * A management interface for managing Flow definition registries at runtime.
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
	 * from there externalized representations.
	 */
	public void refresh();

	/**
	 * Refresh the Flow definition in this registry with the flowId provided,
	 * reloading it from it's externalized representation.
	 * @param flowId the flow to refresh.
	 */
	public void refresh(String flowId);

}