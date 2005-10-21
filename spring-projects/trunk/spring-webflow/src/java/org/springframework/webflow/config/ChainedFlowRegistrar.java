package org.springframework.webflow.config;

import org.springframework.util.Assert;

/**
 * A flow registrar chain for applying an ordered list of Flow Registrar
 * implementations.
 * @author Keith Donald
 */
public class ChainedFlowRegistrar implements FlowRegistrar {

	/**
	 * The flow registrar chain.
	 */
	private FlowRegistrar[] chain;

	/**
	 * Creates a chained flow definition registrar.
	 * @param chain the chain
	 */
	public ChainedFlowRegistrar(FlowRegistrar[] chain) {
		Assert.notEmpty(chain, "There must be at least 1 Flow Registrar in the chain");
		this.chain = chain;
	}

	public ConfigurableFlowRegistry registerFlowDefinitions(ConfigurableFlowRegistry registry) {
		for (int i = 0; i < chain.length; i++) {
			registry = chain[i].registerFlowDefinitions(registry);
		}
		return registry;
	}

}
