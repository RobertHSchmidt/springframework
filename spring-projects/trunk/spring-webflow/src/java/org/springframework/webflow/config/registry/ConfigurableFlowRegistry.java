package org.springframework.webflow.config.registry;


/**
 * A subinterface of FlowRegistry that adds mutable operations for registering
 * new flow definitions. Configurable registries are typically populated at
 * runtime by one or more FlowRegistrars.
 * 
 * @see org.springframework.webflow.config.registry.FlowRegistrar
 * 
 * @author Keith Donald
 */
public interface ConfigurableFlowRegistry extends FlowRegistry {

	/**
	 * Register the flow definition in this registry.
	 * @param flowHolder a holder managing the flow definition to register
	 */
	public void registerFlowDefinition(FlowHolder flowHolder);

}