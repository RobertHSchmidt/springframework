package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A factory bean that produces a populated flow registry using a
 * @{link XmlFlowRegistrar}. This is the simplest implementation to use when
 * using a Spring BeanFactory to deploy a registry of XML-based Flow definitions
 * for execution.
 * <p>
 * By default, a configured flow definition will be assigned a
 * registry identifier equal to the filename of the underlying definition
 * resource, minus the filename extension. For example, a XML-based flow
 * definition defined in the file "flow1.xml" will be identified as "flow1"
 * in the registry created by this factory bean.
 * <p> 
 * This class is also <code>ResourceLoaderAware</code>; when an instance is
 * created by a Spring BeanFactory the factory will automatically configure the
 * XmlFlowRegistrar with a context-relative resource loader for accessing other
 * resources during Flow assembly.
 * 
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;definitionLocations&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;value&gt;/WEB-INF/flow1.xml&lt;/value&gt;
 *                 &lt;value&gt;/WEB-INF/flow2.xml&lt;/value&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean implements ResourceLoaderAware {

	/**
	 * The flow registrar that will perform the definition registrations.
	 */
	private XmlFlowRegistrar flowRegistrar = new XmlFlowRegistrar();

	/**
	 * Creates a xml flow registry factory bean.
	 */
	public XmlFlowRegistryFactoryBean() {
	}

	/**
	 * Creates a xml flow registry factory bean, for programmatic usage only.
	 * @param beanFactory the bean factory to use for locating flow artifacts.
	 */
	public XmlFlowRegistryFactoryBean(BeanFactory beanFactory) {
		super(beanFactory);
		init();
	}

	/**
	 * Returns the configured Xml flow registrar.
	 */
	protected XmlFlowRegistrar getXmlFlowRegistrar() {
		return flowRegistrar;
	}

	/**
	 * Sets the locations (file paths) pointing to XML-based flow definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionLocations(Resource[] locations) {
		getXmlFlowRegistrar().setDefinitionLocations(locations);
	}

	/**
	 * Sets the locations pointing to directories containing XML-based flow
	 * definitions.
	 * @param locations the directory locationsd
	 */
	public void setDefinitionDirectoryLocations(Resource[] locations) {
		getXmlFlowRegistrar().setDefinitionDirectoryLocations(locations);
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		getXmlFlowRegistrar().setResourceLoader(resourceLoader);
	}

	protected void init() {
		getXmlFlowRegistrar().setFlowArtifactFactory(getFlowArtifactFactory());
	}

	protected void doPopulate(FlowRegistry registry) {
		getXmlFlowRegistrar().registerFlowDefinitions(registry);
	}
}