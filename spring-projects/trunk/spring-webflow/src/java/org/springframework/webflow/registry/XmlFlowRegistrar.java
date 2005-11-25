/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.registry;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.config.FlowArtifactFactory;
import org.springframework.webflow.config.FlowAssembler;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * A flow registrar that populates a flow registry from flow definitions defined
 * within XML resources. Typically used in conjunction with a
 * {@link XmlFlowRegistryFactoryBean} but may also be used standalone in
 * programmatic fashion.
 * <p>
 * By default, a flow definition registered by this registrar will be assigned a
 * registry identifier equal to the filename of the underlying definition
 * resource, minus the filename extension. For example, a XML-based flow
 * definition defined in the file "flow1.xml" will be identified as "flow1" when
 * registered in a registry.
 * 
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
	 * The xml file suffix constant.
	 */
	private static final String XML_SUFFIX = ".xml";

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
	private ResourceLoader resourceLoader;

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

	/**
	 * Sets the resource loading strategy to use to load local flow artifacts
	 * during the flow building process.
	 * @param resourceLoader the resource loader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
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
		if (definitionDirectoryLocations != null) {
			for (int i = 0; i < definitionDirectoryLocations.length; i++) {
				try {
					addDirectory(definitionDirectoryLocations[i].getFile(), registry);
				}
				catch (IOException e) {
					// handle?
				}
			}
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
			else if (file.getName().endsWith(XML_SUFFIX)) {
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
		FlowAssembler assembler = new FlowAssembler(getFlowId(location), builder);
		registry.registerFlow(new RefreshingFlowHolder(assembler));
	}

	/**
	 * Calculates the <code>flowId</code> to assign to the Flow definition to
	 * be built from the specified resource location.
	 * @param location ther resource
	 * @return the flow id
	 */
	protected String getFlowId(Resource location) {
		return StringUtils.delete(location.getFilename(), XML_SUFFIX);
	}

	public String toString() {
		return new ToStringCreator(this).append("definitionLocation", definitionLocations).append(
				"definitionDirectoryLocations", definitionDirectoryLocations).append("flowArtifactLocator",
				flowArtifactFactory).toString();
	}
}