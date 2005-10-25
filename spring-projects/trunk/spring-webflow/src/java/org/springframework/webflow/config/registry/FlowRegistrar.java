package org.springframework.webflow.config.registry;

/**
 * A strategy responsible for registering one or more flow definitions in a
 * registry. Flow registrars encapsulate the knowledge about the source of a set
 * of flow definition resources, and the behaivior necessary to add those
 * resources to a flow registry.
 * <p>
 * This design where various FlowRegistrars populate a generic FlowRegistry was
 * inspired by Spring's GenericApplicationContext, which can use any number of
 * BeanDefinitionReaders to drive context population.
 * 
 * @see FlowRegistry
 * @see FlowRegistrarImpl
 * @see XmlFlowRegistrar
 * 
 * @author Keith Donald
 */
public interface FlowRegistrar {

	/**
	 * Register flow definition resources managed by this registrar in the
	 * registry provided.
	 * @param registry the registry to register flow definitions in
	 */
	public void registerFlowDefinitions(FlowRegistry registry);
}