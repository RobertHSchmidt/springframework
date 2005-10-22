package org.springframework.webflow.config.registry;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.config.FlowArtifactLocator;
import org.springframework.webflow.config.FlowBuilderException;
import org.springframework.webflow.config.JarFileResource;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * A Flow Registrar that can register refreshable flow definitions from loaded
 * from XML resources.
 * @author Keith Donald
 */
public class XmlFlowRegistrar implements FlowRegistrar {

	/**
	 * XML flow definition resources to load.
	 */
	private Resource[] definitionLocations;

	/**
	 * JAR files containing XML flow definition resources to load.
	 */
	private Resource[] definitionJarLocations;

	/**
	 * Directory locations containing XML flow definition resources to load.
	 */
	private Resource[] definitionDirectoryLocations;

	/**
	 * Strategy for locating dependent artifacts when a Flow is being built.
	 */
	private FlowArtifactLocator artifactLocator;

	/**
	 * Creates an XML flow registrar
	 * @param artifactLocator the flow artifact locator
	 */
	protected XmlFlowRegistrar() {

	}

	/**
	 * Creates an XML flow registrar
	 * @param artifactLocator the flow artifact locator
	 */
	public XmlFlowRegistrar(FlowArtifactLocator artifactLocator) {
		setFlowArtifactLocator(artifactLocator);
	}

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
	 * Sets the strategy for locating dependent artifacts when a Flow is being
	 * built.
	 * @param artifactLocator the flow artifact locator
	 */
	public void setFlowArtifactLocator(FlowArtifactLocator artifactLocator) {
		Assert.notNull(artifactLocator, "The flow artifact locator is required");
		this.artifactLocator = artifactLocator;
	}

	public void registerFlowDefinitions(FlowRegistry registry) {
		registerDefinitions(registry);
		registerJarDefinitions(registry);
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
	 * Register the Flow definitions at the configured jar file locations.
	 */
	protected void registerJarDefinitions(FlowRegistry registry) {
		JarFile jar = null;
		try {
			if (definitionJarLocations != null) {
				for (int i = 0; i < definitionJarLocations.length; i++) {
					jar = new JarFile(definitionJarLocations[i].getFile());
					Enumeration entries = jar.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry)entries.nextElement();
						if (entry.getName().endsWith(".xml")) {
							registerFlow(new JarFileResource(jar, entry), registry);
						}
					}
				}
			}
		}
		catch (IOException e) {
			throw new FlowBuilderException("Unable to build Flows from jar definitions", e);
		}
		finally {
			if (jar != null) {
				try {
					jar.close();
				}
				catch (IOException e) {

				}
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
		registry.registerFlowDefinition(new FlowAssembler(new XmlFlowBuilder(location, artifactLocator)));
	}

	public String toString() {
		return new ToStringCreator(this).append("definitionLocation", definitionLocations).append(
				"definitionJarLocations", definitionJarLocations).append("definitionDirectoryLocations",
				definitionDirectoryLocations).append("flowArtifactLocator", artifactLocator).toString();
	}
}