package org.springframework.webflow.config.registry;

/**
 * A strategy responsible for registering one or more Flow definitions in a Flow
 * Registry. Encapsulates knowledge about the source and content of those Flow
 * definitions.
 * 
 * @author Keith Donald
 */
public interface FlowRegistrar {

	/**
	 * Register flow definitions managed by this registrar in the registry
	 * provided.
	 * @param registry the configurable registry to register flow definitions in
	 * @return the same registry
	 */
	public void registerFlowDefinitions(ConfigurableFlowRegistry registry);
}