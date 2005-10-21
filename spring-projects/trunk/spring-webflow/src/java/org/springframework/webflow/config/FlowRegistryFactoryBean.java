package org.springframework.webflow.config;

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
	private ConfigurableFlowRegistry registry = new FlowRegistryImpl();

	/**
	 * The Flow Registrar that will perform the definition registrations.
	 */
	private FlowRegistrar registrar;

	/**
	 * Creates a new factory bean that will populate the Flow Registry using the
	 * provided registrar
	 * @param registrar the Flow definition registrar
	 */
	public FlowRegistryFactoryBean(FlowRegistrar registrar) {
		Assert.notNull(registrar, "The Flow Registrar to perform registrations is required");
		this.registrar = registrar;
	}

	public Object getObject() throws Exception {
		return registrar.registerFlowDefinitions(registry);
	}

	public Class getObjectType() {
		return FlowRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}
}