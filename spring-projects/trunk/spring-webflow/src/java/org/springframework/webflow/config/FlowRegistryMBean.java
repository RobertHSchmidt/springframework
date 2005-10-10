package org.springframework.webflow.config;

/**
 * A management interface for controller Flow definition registries at runtime.
 * This interface follows JMX "MBean" naming conventions for easy implementation
 * registration as a JMX 1.0 MBean.
 * @author Keith Donald
 */
public interface FlowRegistryMBean {

	/**
	 * Refresh this flow definition registry, reloading all Flow definitions
	 * from there externalized representations.
	 */
	public abstract void refresh();

	/**
	 * Refresh the Flow definition in this registry with the flowId provided,
	 * reloading it from it's externalized representation.
	 * @param flowId the flow to refresh.
	 */
	public abstract void refresh(String flowId);

}