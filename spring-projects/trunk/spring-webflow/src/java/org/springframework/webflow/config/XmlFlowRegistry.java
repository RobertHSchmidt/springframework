package org.springframework.webflow.config;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
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
public class XmlFlowRegistry implements FlowLocator, FlowRegistryMBean, InitializingBean, BeanFactoryAware {

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions = new HashMap();

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
	 * Creates an initially empty flow registry.
	 */
	public XmlFlowRegistry() {

	}

	/**
	 * Creates an initially empty flow registry.
	 * @param artifactLocator the flow artifact locator
	 */
	public XmlFlowRegistry(FlowArtifactLocator artifactLocator) {
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
		refresh();
	}

	public void refresh() {
		loadDefinitions();
		loadJarDefinitions();
		loadDirectoryDefinitions();
	}

	protected void loadDefinitions() {
		if (definitionLocations != null) {
			for (int i = 0; i < definitionLocations.length; i++) {
				loadFlow(definitionLocations[i]);
			}
		}
	}

	protected void loadJarDefinitions() throws FlowBuilderException {
		JarFile jar = null;
		try {
			if (definitionJarLocations != null) {
				for (int i = 0; i < definitionJarLocations.length; i++) {
					jar = new JarFile(definitionJarLocations[i].getFile());
					Enumeration jarEntries = jar.entries();
					while (jarEntries.hasMoreElements()) {
						ZipEntry ze = (ZipEntry)jarEntries.nextElement();
						if (ze.getName().endsWith(".xml")) {
							loadFlow(new InputStreamResource(jar.getInputStream(ze)));
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

	protected void loadDirectoryDefinitions() {
		try {
			if (definitionDirectoryLocations != null) {
				for (int i = 0; i < definitionDirectoryLocations.length; i++) {
					addDirectory(definitionDirectoryLocations[i].getFile());
				}
			}
		}
		catch (IOException e) {
			throw new FlowBuilderException("Unable to build Flows from jar definitions", e);
		}
	}

	protected void addDirectory(File directory) {
		Assert.isTrue(directory.isDirectory(), "The file must be a directory, programmer error");
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				addDirectory(file);
			}
			else if (file.getName().endsWith(".xml")) {
				addFile(file);
			}
		}
	}

	protected void addFile(File file) {
		loadFlow(new FileSystemResource(file));
	}

	/**
	 * Load the Flow definition from the XML resource provided and register it
	 * in this registry.
	 * @param resource the XML resource
	 * @throws FlowBuilderException the builder could not build the Flow
	 */
	protected void loadFlow(Resource resource) throws FlowBuilderException {
		registerFlowDefinition(new RefreshableFlow(resource));
	}

	private void registerFlowDefinition(RefreshableFlow flow) {
		this.flowDefinitions.put(flow.getFlow().getId(), flow);
	}

	/**
	 * Register the flow definition in this registry.
	 * @param flow The flow to register
	 */
	public void registerFlowDefinition(Flow flow) {
		registerFlowDefinition(new RefreshableFlow(flow));
	}

	public void refresh(String flowId) {
		getRefreshableFlow(flowId).refresh();
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		return getRefreshableFlow(id).getFlow();
	}

	private RefreshableFlow getRefreshableFlow(String id) {
		RefreshableFlow flow = (RefreshableFlow)flowDefinitions.get(id);
		if (flow == null) {
			throw new NoSuchFlowDefinitionException(id);
		}
		return flow;
	}

	/**
	 * A value object holding a Flow definition and a pointer to its resource to
	 * support a refresh operation.
	 * @author Keith Donald
	 */
	protected class RefreshableFlow {
		private Flow flow;

		private Resource location;

		public RefreshableFlow(Flow flow) {
			this.flow = flow;
		}

		public RefreshableFlow(Resource location) {
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
				FlowBuilder builder = new XmlFlowBuilder(location, artifactLocator);
				this.flow = builder.init();
				builder.buildStates();
				builder.dispose();
			}
		}

		public String toString() {
			return new ToStringCreator(this).append("flow", getFlow()).append("location", getLocation()).toString();
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("flowDefinitions", flowDefinitions).toString();
	}
}