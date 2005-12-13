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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.XmlFlowBuilder;

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
 *     BeanFactory beanFactory = ...
 *     FlowRegistryImpl registry = new FlowRegistryImpl();
 *     FlowArtifactFactory flowArtifactFactory =
 *         new FlowRegistryFlowArtifactFactory(registry, beanFactory);
 *     File parent = new File(&quot;src/webapp/WEB-INF&quot;);
 *     Resource[] locations = new Resource[] {
 *         new FileSystemResource(new File(parent, &quot;flow1.xml&quot;)),
 *         new FileSystemResource(new File(parent, &quot;flow2.xml&quot;))
 *     };
 *     new XmlFlowRegistrar(locations).registerFlows(locations, flowArtifactFactory);
 * </pre>
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistrar extends FlowRegistrarSupport {

	private static final Log logger = LogFactory.getLog(XmlFlowRegistrar.class);

	/**
	 * The xml file suffix constant.
	 */
	private static final String XML_SUFFIX = ".xml";

	/**
	 * XML flow definition resources to load.
	 */
	private Set flowLocations = new HashSet();

	/**
	 * Directory locations containing XML flow definition resources to load.
	 */
	private Set flowDirectoryLocations = new HashSet();

	/**
	 * Creates an XML flow registrar.
	 */
	public XmlFlowRegistrar() {
	}

	/**
	 * Creates an XML flow registrar.
	 * @param definitionLocations the XML flow definition resource locations
	 */
	public XmlFlowRegistrar(Resource[] definitionLocations) {
		setFlowLocations(definitionLocations);
	}

	/**
	 * Sets the locations (file paths) pointing to XML-based flow definitions.
	 * @param flowLocations the resource locations
	 */
	public void setFlowLocations(Resource[] flowLocations) {
		this.flowLocations = new HashSet(Arrays.asList(flowLocations));
	}

	/**
	 * Sets the locations pointing to directories containing XML-based flow
	 * definitions.
	 * @param flowDirectoryLocations the directory locationsd
	 */
	public void setFlowDirectoryLocations(Resource[] flowDirectoryLocations) {
		this.flowDirectoryLocations = new HashSet(Arrays.asList(flowDirectoryLocations));
	}

	/**
	 * Adds a flow location pointing to an XML-based flow definition.
	 * @param flowLocation the definition location
	 */
	public void addFlowLocation(Resource flowLocation) {
		this.flowLocations.add(flowLocation);
	}

	/**
	 * Adds flow locations pointing to a set of XML-based flow definitions.
	 * @param flowLocations the definition locations
	 */
	public void addFlowLocations(Resource[] flowLocations) {
		if (flowLocations == null) {
			return;
		}
		this.flowLocations.addAll(Arrays.asList(flowLocations));
	}

	public void registerFlows(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		registerDefinitions(registry, flowArtifactFactory);
		registerDirectoryDefinitions(registry, flowArtifactFactory);
	}

	/**
	 * Register the Flow definitions at the configured file locations
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void registerDefinitions(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Iterator it = flowLocations.iterator();
		while (it.hasNext()) {
			registerFlow((Resource)it.next(), registry, flowArtifactFactory);
		}
	}

	/**
	 * Register the Flow definitions at the configured directory locations,
	 * automatically adding flow definitions in any subdirectories.
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void registerDirectoryDefinitions(FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Iterator it = flowDirectoryLocations.iterator();
		while (it.hasNext()) {
			Resource resource = (Resource)it.next();
			try {
				addDirectory(resource.getFile(), registry, flowArtifactFactory);
			}
			catch (IOException e) {
				logger.warn("Unable to add directory resource " + resource + ", skipping", e);
			}
		}
	}

	/**
	 * Adds the flow definitions in the provided directory to the registry.
	 * @param directory the directory containing flow definitions
	 * @param registry the flow registry
	 * @param flowArtifactFactory the flow artifact factory
	 */
	protected void addDirectory(File directory, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		Assert.isTrue(directory.isDirectory(), "The file must be a directory, programmer error");
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				addDirectory(file, registry, flowArtifactFactory);
			}
			else if (isFlowDefinition(file)) {
				addFile(file, registry, flowArtifactFactory);
			}
		}
	}

	/**
	 * Template method that calculates if the given file resource is actually a
	 * flow definition resource.
	 * @param file the file
	 * @return true if yes, false otherwise
	 */
	protected boolean isFlowDefinition(File file) {
		return file.getName().endsWith(XML_SUFFIX);
	}

	/**
	 * Adds the flow definitions in the provided file to the registry.
	 * @param file the file containing a flow definition
	 * @param registry the flow registry
	 * @param flowArtifactFactory the flow artifact factory
	 */
	protected void addFile(File file, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		registerFlow(new FileSystemResource(file), registry, flowArtifactFactory);
	}

	/**
	 * Register the Flow definition from the XML resource provided in the
	 * provided registry.
	 * @param location the XML resource
	 * @param registry the registry
	 * @param flowArtifactFactory the flow artifactFactory
	 */
	protected void registerFlow(Resource location, FlowRegistry registry, FlowArtifactFactory flowArtifactFactory) {
		registerFlow(getFlowId(location), new XmlFlowBuilder(location, flowArtifactFactory), registry);
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
		return new ToStringCreator(this).append("flowLocations", flowLocations).append(
				"flowDirectoryLocations", flowDirectoryLocations).toString();
	}
}