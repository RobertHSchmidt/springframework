package org.springframework.webflow.config.registry;

/**
 * A strategy responsible for registering one or more Flow definitions in a Flow
 * Registry. Encapsulates knowledge and behaivior regarding the source of those
 * flow definitions.
 * 
 * @see org.springframework.webflow.config.registry.ConfigurableFlowRegistry
 * 
 * @author Keith Donald
 */
public interface FlowRegistrar {

	/**
	 * Register flow definitions managed by this registrar in the registry
	 * provided.
	 * @param registry the configurable registry to register flow definitions in
	 */
	public void registerFlowDefinitions(ConfigurableFlowRegistry registry);
}