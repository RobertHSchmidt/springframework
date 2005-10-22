package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;

/**
 * A factory bean that produces a populated Flow Registry using a XML flow
 * definition registrar.
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {

	/**
	 * The flow registrar that will perform the definition registrations.
	 */
	private XmlFlowRegistrar registrar = new XmlFlowRegistrar();

	/**
	 * Creates a xml flow registry factory bean.
	 */
	public XmlFlowRegistryFactoryBean() {
	}

	/**
	 * Creates a xml flow registry factory bean.
	 * @param beanFactory the bean factory to use for locating flow artifacts.
	 */
	public XmlFlowRegistryFactoryBean(BeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * Returns the configured Xml flow registrar.
	 */
	protected XmlFlowRegistrar getXmlFlowRegistrar() {
		return registrar;
	}

	/**
	 * Sets the locations (file paths) pointing to XML-based flow definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionLocations(Resource[] locations) {
		getXmlFlowRegistrar().setDefinitionLocations(locations);
	}

	/**
	 * Sets the locations pointing to JAR files containing XML-based flow
	 * definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionJarLocations(Resource[] locations) {
		getXmlFlowRegistrar().setDefinitionJarLocations(locations);
	}

	/**
	 * Sets the locations pointing to directories containing XML-based flow
	 * definitions.
	 * @param locations the directory locationsd
	 */
	public void setDefinitionDirectoryLocations(Resource[] locations) {
		getXmlFlowRegistrar().setDefinitionDirectoryLocations(locations);
	}

	protected void init() {
		getXmlFlowRegistrar().setFlowArtifactLocator(getFlowArtifactLocator());
	}
	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public void registerFlowDefinitions(FlowRegistry registry) {
		getXmlFlowRegistrar().registerFlowDefinitions(registry);
	}
}