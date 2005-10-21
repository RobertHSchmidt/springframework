package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * A factory bean that produces a populated Flow Registry.
 * 
 * @author Keith Donald
 */
public class FlowRegistryFactoryBean implements FactoryBean {

	/**
	 * The Flow Registry to register Flow definitions in.
	 */
	private FlowRegistry registry = new FlowRegistryImpl();

	/**
	 * The Flow Registrar that will perform the definition registrations.
	 */
	private FlowRegistrar registrar;

	/**
	 * Creates an initially unconfigured factory bean.
	 */
	protected FlowRegistryFactoryBean() {
		
	}
	
	/**
	 * Creates a new factory bean that will populate a default Flow Registry
	 * using the provided registrar
	 * @param registrar the Flow definition registrar
	 */
	public FlowRegistryFactoryBean(FlowRegistrar registrar) {
		Assert.notNull(registrar, "The Flow Registrar to perform registrations is required");
		this.registrar = registrar;
	}

	/**
	 * Creates a new factory bean that will populate the provided Flow Registry
	 * using the provided registrar
	 * @param registrar the Flow definition registrar
	 * @param registry the Flow definition registry
	 */
	public FlowRegistryFactoryBean(FlowRegistrar registrar, FlowRegistry registry) {
		Assert.notNull(registrar, "The Flow Registrar to perform registrations is required");
		Assert.notNull(registry, "The Flow Registry to accept registrations is required");
		this.registrar = registrar;
		this.registry = registry;
	}

	/**
	 * Returns the flow registry that will accept Flow definitions.
	 */
	protected FlowRegistry getFlowRegistry() {
		return registry;
	}
	
	/**
	 * Returns the flow registrar that will perform Flow definition registries.
	 */
	protected FlowRegistrar getFlowRegistrar() {
		return registrar;
	}
	
	protected void setFlowRegistrar(FlowRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public FlowRegistry populateFlowRegistry() {
		registrar.registerFlowDefinitions(registry);
		return registry;
	}
	
	public Class getObjectType() {
		return FlowRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}
}