/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.builder.xml;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.registry.ExternalizedFlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.RefreshableFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.support.FlowServiceLocator;

/**
 * A flow definition registrar that populates a flow definition registry with flow definitions defined in externalized
 * XML resources.
 * <p>
 * By default, a flow definition added to this registrar with the {@link #addLocation(Resource, String)} method will be
 * will be assigned a registry identifier equal to the filename of the underlying definition resource, minus the
 * filename extension. For example, a XML-based flow definition defined in the file "flow1.xml" will be identified as
 * "flow1" when registered in a registry.
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistrar extends ExternalizedFlowDefinitionRegistrar {

	/**
	 * The loader of XML-based flow definition documents.
	 */
	private DocumentLoader documentLoader;

	/**
	 * The service locator needed for xml flow definition assembly.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * Creates a new XML flow registrar.
	 * @param flowServiceLocator the locator needed to support xml flow definition assembly
	 */
	public XmlFlowRegistrar(FlowServiceLocator flowServiceLocator) {
		Assert.notNull(flowServiceLocator, "The flow service locator is required");
		this.flowServiceLocator = flowServiceLocator;
	}

	/**
	 * Sets the loader to load XML-based flow definition documents during flow definition assembly. Allows for
	 * customization over how documents are loaded. Optional.
	 * @param documentLoader the document loader
	 */
	public void setDocumentLoader(DocumentLoader documentLoader) {
		this.documentLoader = documentLoader;
	}

	// hook methods

	protected FlowDefinitionHolder createFlowDefinitionHolder(FlowDefinitionResource resource) {
		FlowBuilder builder = createFlowBuilder(resource.getLocation());
		FlowAssembler assembler = new FlowAssembler(resource.getId(), builder, resource.getAttributes());
		return new RefreshableFlowDefinitionHolder(assembler);
	}

	/**
	 * Factory method that creates and fully initializes the XML-based flow definition builder.
	 * @param location the xml-based resource
	 * @return the builder to build the flow definition from the resource.
	 */
	protected FlowBuilder createFlowBuilder(Resource location) {
		XmlFlowBuilder builder = new XmlFlowBuilder(location, flowServiceLocator);
		if (documentLoader != null) {
			builder.setDocumentLoader(documentLoader);
		}
		return builder;
	}
}