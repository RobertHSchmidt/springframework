/*
 * Copyright 2002-2006 the original author or authors.
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

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.XmlFlowBuilder;

/**
 * A flow registrar that populates a flow registry from flow definitions defined
 * within externalized XML resources. Typically used in conjunction with a
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
 *    BeanFactory beanFactory = ...
 *    FlowRegistryImpl registry = new FlowRegistryImpl();
 *    FlowArtifactFactory flowArtifactFactory =
 *        new FlowRegistryFlowArtifactFactory(registry, beanFactory);
 *    XmlFlowRegistrar registrar = new XmlFlowRegistrar();
 *    File parent = new File(&quot;src/webapp/WEB-INF&quot;);
 *    registrar.addFlowLocation(new FileSystemResource(new File(parent, &quot;flow1.xml&quot;));
 *    registrar.addFlowLocation(new FileSystemResource(new File(parent, &quot;flow2.xml&quot;));
 *    registrar.registerFlows(locations, flowArtifactFactory);
 * </pre>
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistrar extends ExternalizedFlowRegistrar {

	/**
	 * The xml file suffix constant.
	 */
	private static final String XML_SUFFIX = ".xml";

	protected boolean isFlowDefinition(File file) {
		return file.getName().endsWith(XML_SUFFIX);
	}

	protected FlowBuilder createFlowBuilder(Resource location, FlowArtifactFactory flowArtifactFactory) {
		return new XmlFlowBuilder(location, flowArtifactFactory);
	}

	protected String getFlowId(Resource location) {
		return StringUtils.delete(location.getFilename(), XML_SUFFIX);
	}
}