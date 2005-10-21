package org.springframework.webflow.config.registry;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.BeanFactoryFlowArtifactLocator;
import org.springframework.webflow.config.FlowArtifactLocator;
import org.springframework.webflow.config.FlowAssembler;
import org.springframework.webflow.config.FlowBuilderException;
import org.springframework.webflow.config.JarFileResource;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * A Flow Registrar that can register refreshable flow definitions from loaded
 * from XML resources.
 * @author Keith Donald
 */
public class XmlFlowRegistrar implements FlowRegistrar, BeanFactoryAware {

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
	 */
	public XmlFlowRegistrar() {
		
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
		this.artifactLocator = artifactLocator;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (artifactLocator == null) {
			artifactLocator = new BeanFactoryFlowArtifactLocator(beanFactory);
		}
	}
	
	public ConfigurableFlowRegistry registerFlowDefinitions(ConfigurableFlowRegistry registry) {
		registerDefinitions(registry);
		registerJarDefinitions(registry);
		registerDirectoryDefinitions(registry);
		return registry;
	}

	/**
	 * Register the Flow definitions at the configured file locations.
	 */
	protected void registerDefinitions(ConfigurableFlowRegistry registry) {
		if (definitionLocations != null) {
			for (int i = 0; i < definitionLocations.length; i++) {
				registerFlow(definitionLocations[i], registry);
			}
		}
	}

	/**
	 * Register the Flow definitions at the configured jar file locations.
	 */
	protected void registerJarDefinitions(ConfigurableFlowRegistry registry) {
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
	protected void registerDirectoryDefinitions(ConfigurableFlowRegistry registry) {
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

	protected void addDirectory(File directory, ConfigurableFlowRegistry registry) {
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

	protected void addFile(File file, ConfigurableFlowRegistry registry) {
		registerFlow(new FileSystemResource(file), registry);
	}

	/**
	 * Register the Flow definition from the XML resource provided in the
	 * provided registry.
	 * @param resource the XML resource
	 */
	protected void registerFlow(Resource resource, ConfigurableFlowRegistry registry) {
		registry.registerFlowDefinition(new RefreshableXmlFlowHolder(resource));
	}

	/**
	 * Holds a Flow definition and a pointer to its originating XML resource to
	 * support a refresh operation.
	 * @author Keith Donald
	 */
	public class RefreshableXmlFlowHolder implements RefreshableFlowHolder {

		/**
		 * The Flow definition held by this holder.
		 */
		private Flow flow;

		/**
		 * The Flow definition resource, a XML file.
		 */
		private Resource location;

		/**
		 * @param artifactLocator
		 * @param location
		 */
		public RefreshableXmlFlowHolder(Resource location) {
			this.location = location;
			refresh();
		}

		public Flow getFlow() {
			return flow;
		}

		public Resource getLocation() {
			return location;
		}

		public void refresh() {
			if (location != null) {
				this.flow = new FlowAssembler(new XmlFlowBuilder(location, artifactLocator)).getFlow();
			}
		}

		public String toString() {
			return new ToStringCreator(this).append("flow", getFlow()).append("location", getLocation()).toString();
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("definitionLocation", definitionLocations).append(
				"definitionJarLocations", definitionJarLocations).append("definitionDirectoryLocations",
				definitionDirectoryLocations).toString();
	}
}