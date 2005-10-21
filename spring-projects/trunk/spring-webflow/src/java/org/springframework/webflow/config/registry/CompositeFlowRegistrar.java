package org.springframework.webflow.config.registry;

import org.springframework.util.Assert;

/**
 * A flow registrar that applies an ordered list of Flow Registrar
 * implementations as part of a chain.
 * @author Keith Donald
 */
public class CompositeFlowRegistrar implements FlowRegistrar {

	/**
	 * The flow registrar chain.
	 */
	private FlowRegistrar[] registrars;

	/**
	 * Creates a chained flow definition registrar.
	 * @param registrars the chain
	 */
	public CompositeFlowRegistrar(FlowRegistrar[] registrars) {
		Assert.notEmpty(registrars, "There must be at least 1 Flow Registrar in the chain");
		this.registrars = registrars;
	}

	public void registerFlowDefinitions(FlowRegistry registry) {
		for (int i = 0; i < registrars.length; i++) {
			registrars[i].registerFlowDefinitions(registry);
		}
	}
}