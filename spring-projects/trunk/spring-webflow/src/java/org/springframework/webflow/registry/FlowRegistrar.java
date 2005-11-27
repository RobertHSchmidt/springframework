package org.springframework.webflow.registry;

import org.springframework.webflow.builder.FlowArtifactFactory;

/**
 * A strategy to use to populate a flow registry with one or more flow
 * definitions.
 * <p>
 * Flow registrars encapsulate the knowledge about the source of a set of flow
 * definition resources, and the behavior necessary to add those resources to a
 * flow registry.
 * <p>
 * This design where various FlowRegistrars populate a generic FlowRegistry was
 * inspired by Spring's GenericApplicationContext, which can use any number of
 * BeanDefinitionReaders to drive context population.
 * <p>
 * The typical usage pattern is as follows:
 * <ol>
 * <li>Create a new (initially empty) flow registry.
 * <li>Create a flow artifact factory to create flow artifacts during the flow
 * registration process.
 * <li>Use any number of flow registrars to populate the registry by calling
 * {@link #registerFlows(FlowRegistry, FlowArtifactFactory)}.
 * </ol>
 * </p>
 * @see FlowRegistry
 * @see FlowArtifactFactory
 * @see FlowRegistrarSupport
 * @see XmlFlowRegistrar
 * 
 * @author Keith Donald
 */
public interface FlowRegistrar {

	/**
	 * Register flow definition resources managed by this registrar in the
	 * registry provided.
	 * @param registry the registry to register flow definitions in
	 * @param flowArtifactFactory the flow artifact factory for accessing externally managed flow 
	 * artifacts, typically used by flow builders that build flow definitions
	 */
	public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory);
}