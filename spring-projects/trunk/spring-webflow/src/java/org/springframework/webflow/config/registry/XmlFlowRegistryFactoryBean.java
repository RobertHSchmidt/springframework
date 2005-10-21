package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.Resource;

/**
 * A factory bean that produces a populated Flow Registry using a XML flow
 * definition registrar.
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistryFactoryBean extends FlowRegistryFactoryBean implements BeanFactoryAware {

	/**
	 * Creates a new factory bean that will populate a default Flow Registry
	 * using the provided registrar
	 * @param registrar the Flow definition registrar
	 */
	public XmlFlowRegistryFactoryBean() {
		super(new XmlFlowRegistrar());
	}

	/**
	 * Creates a new factory bean that will populate the provided Flow Registry
	 * using the provided registrar
	 * @param registrar the Flow definition registrar
	 * @param registry the Flow definition registry
	 */
	public XmlFlowRegistryFactoryBean(ConfigurableFlowRegistry registry) {
		super(new XmlFlowRegistrar(), registry);
	}

	/**
	 * Returns the configured Xml flow registrar.
	 */
	protected XmlFlowRegistrar getXmlFlowRegistrar() {
		return (XmlFlowRegistrar)getFlowRegistrar();
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

	public void setBeanFactory(BeanFactory beanFactory) {
		getXmlFlowRegistrar().setBeanFactory(beanFactory);
	}
}