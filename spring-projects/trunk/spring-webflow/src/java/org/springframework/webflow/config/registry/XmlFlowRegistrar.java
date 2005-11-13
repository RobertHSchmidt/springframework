package org.springframework.webflow.config.registry;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.config.FlowArtifactFactory;
import org.springframework.webflow.config.FlowBuilderException;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * A flow registrar that populates a flow registry from flow definitions defined
 * within XML resources. Typically used in conjunction with a
 * {@link XmlFlowRegistryFactoryBean} but may also be used standalone in
 * programmatic fashion.
 * <p>
 * Programmatic usage example:
 * </p>
 * 
 * <pre>
 * FlowRegistryImpl registry = new FlowRegistryImpl();
 * File parent = new File(&quot;src/webapp/WEB-INF&quot;);
 * Resource[] locations = new Resource[] { new FileSystemResource(new File(parent, &quot;flow1.xml&quot;)),
 * 		new FileSystemResource(new File(parent, &quot;flow2.xml&quot;)) };
 * XmlFlowRegistrar registrar = new XmlFlowRegistrar(flowArtifactLocator, locations);
 * registrar.registerFlowDefinitions(registry);
 * </pre>
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistrar implements FlowRegistrar {

	/**
	 * XML flow definition resources to load.
	 */
	private Resource[] definitionLocations;

	/**
	 * Directory locations containing XML flow definition resources to load.
	 */
	private Resource[] definitionDirectoryLocations;

	/**
	 * Strategy for locating dependent artifacts when a Flow is being built.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Strategy for loading depenent resources needed by the Flow while it is
	 * being built.
	 */
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * Creates an XML flow registrar.
	 */
	protected XmlFlowRegistrar() {

	}

	/**
	 * Creates an XML flow registrar.
	 * @param artifactLocator the flow artifact locator that will find artifacts
	 * needed by Flows registered by this registrar
	 */
	public XmlFlowRegistrar(FlowArtifactFactory artifactLocator) {
		setFlowArtifactFactory(artifactLocator);
	}

	/**
	 * Creates an XML flow registrar.
	 * @param artifactLocator the flow artifact locator that will find artifacts
	 * needed by Flows registered by this registrar
	 * @param definitionLocations the XML flow definition resource locations
	 */
	public XmlFlowRegistrar(FlowArtifactFactory artifactLocator, Resource[] definitionLocations) {
		setFlowArtifactFactory(artifactLocator);
		setDefinitionLocations(definitionLocations);
	}

	/**
	 * Sets the locations (file paths) pointing to XML-based flow definitions.
	 * @param locations the resource locations
	 */
	public void setDefinitionLocations(Resource[] locations) {
		this.definitionLocations = locations;
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
	 * Sets the strategy for locating dependent artifacts when a Flow is being
	 * built.
	 * @param artifactLocator the flow artifact locator
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory artifactLocator) {
		Assert.notNull(artifactLocator, "The flow artifact locator is required");
		this.flowArtifactFactory = artifactLocator;
	}

	public void registerFlowDefinitions(FlowRegistry registry) {
		registerDefinitions(registry);
		registerDirectoryDefinitions(registry);
	}

	/**
	 * Register the Flow definitions at the configured file locations.
	 */
	protected void registerDefinitions(FlowRegistry registry) {
		if (definitionLocations != null) {
			for (int i = 0; i < definitionLocations.length; i++) {
				registerFlow(definitionLocations[i], registry);
			}
		}
	}

	/**
	 * Register the Flow definitions at the configured directory locations.
	 */
	protected void registerDirectoryDefinitions(FlowRegistry registry) {
		try {
			if (definitionDirectoryLocations != null) {
				for (int i = 0; i < definitionDirectoryLocations.length; i++) {
					addDirectory(definitionDirectoryLocations[i].getFile(), registry);
				}
			}
		}
		catch (IOException e) {
			throw new FlowBuilderException("Unable to build Flows from jar definitions", e);
		}
	}

	protected void addDirectory(File directory, FlowRegistry registry) {
		Assert.isTrue(directory.isDirectory(), "The file must be a directory, programmer error");
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				addDirectory(file, registry);
			}
			else if (file.getName().endsWith(".xml")) {
				addFile(file, registry);
			}
		}
	}

	protected void addFile(File file, FlowRegistry registry) {
		registerFlow(new FileSystemResource(file), registry);
	}

	/**
	 * Register the Flow definition from the XML resource provided in the
	 * provided registry.
	 * @param location the XML resource
	 */
	protected void registerFlow(Resource location, FlowRegistry registry) {
		XmlFlowBuilder builder = new XmlFlowBuilder(location, flowArtifactFactory);
		builder.setResourceLoader(resourceLoader);
		registry.registerFlowDefinition(new FlowAssembler(builder));
	}

	public String toString() {
		return new ToStringCreator(this).append("definitionLocation", definitionLocations).append(
				"definitionDirectoryLocations", definitionDirectoryLocations).append("flowArtifactLocator",
				flowArtifactFactory).toString();
	}
}