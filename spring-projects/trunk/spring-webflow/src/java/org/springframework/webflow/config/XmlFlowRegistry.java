package org.springframework.webflow.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.access.FlowArtifactLookupException;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.access.NoSuchFlowDefinitionException;

/**
 * A refreshable registry of XML Flow definitions. This registry loads its Flow
 * definitions from a set of resources when initialized. It may also be
 * refreshed at runtime to support "hot reloading" of externalized Flow
 * definitions.
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistry implements FlowLocator, InitializingBean, BeanFactoryAware {

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions = Collections.EMPTY_MAP;

	/**
	 * XML flow definition resources.
	 */
	private Resource[] definitionLocations;

	/**
	 * JAR files containing XML flow definition resources.
	 */
	private Resource[] definitionJarLocations;

	/**
	 * Directory locations containing XML flow definition resources.
	 */
	private Resource[] definitionDirectoryLocations;

	/**
	 * Strategy for locating dependent artifacts when a Flow is being built.
	 */
	private FlowArtifactLocator artifactLocator;

	/**
	 * Sets the locations (file paths) pointing to XML-based flow definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionLocations(Resource[] locations) {
		this.definitionLocations = locations;
	}

	/**
	 * Sets the locations pointing to JAR files containing XML-based flow
	 * definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionJarLocations(Resource[] locations) {
		this.definitionJarLocations = locations;
	}

	/**
	 * Sets the locations pointing to directories containing XML-based flow
	 * definitions.
	 * @param locations the directory locationsd
	 */
	public void setDefinitionDirectoryLocations(Resource[] locations) {
		this.definitionDirectoryLocations = locations;
	}

	/**
	 * Sets the flow artifact locator.
	 * @param artifactLocator the locator
	 */
	public void setFlowArtifactLocator(FlowArtifactLocator artifactLocator) {
		this.artifactLocator = artifactLocator;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.artifactLocator == null) {
			this.artifactLocator = new BeanFactoryFlowArtifactLocator(beanFactory, this);
		}
	}

	public void afterPropertiesSet() throws IOException {
		if (definitionLocations != null) {
			for (int i = 0; i < definitionLocations.length; i++) {
				loadFlow(definitionLocations[i]);
			}
		}
	}

	/**
	 * Load the Flow definition from the XML resource provided and register it
	 * in this registry.
	 * @param resource the XML resource
	 * @throws FlowBuilderException the builder could not build the Flow
	 */
	protected void loadFlow(Resource resource) throws FlowBuilderException {
		FlowBuilder builder = new XmlFlowBuilder(resource);
		// must register first to support recursive flows
		registerFlowDefinition(builder.init());
		builder.buildStates();
		builder.dispose();
	}

	/**
	 * Register the flow definition in this registry.
	 * @param flow The flow to register
	 */
	public void registerFlowDefinition(Flow flow) {
		this.flowDefinitions.put(flow.getId(), flow);
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		Flow flow = (Flow)flowDefinitions.get(id);
		if (flow == null) {
			throw new NoSuchFlowDefinitionException(flow.getId());
		}
		return flow;
	}

	/**
	 * Refresh this flow definition registry, reloading all Flow definitions
	 * from there externalized representations.
	 */
	public void refresh() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Refresh the Flow definition in this registry with the flowId provided,
	 * reloading it from it's externalized representation.
	 * @param flowId the flow to refresh.
	 */
	public void refresh(String flowId) {
		throw new UnsupportedOperationException();
	}
}