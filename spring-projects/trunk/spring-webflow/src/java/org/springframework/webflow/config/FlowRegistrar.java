package org.springframework.webflow.config;

/**
 * A worker object who knows how to register a set of flow definitions in a
 * registry.
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