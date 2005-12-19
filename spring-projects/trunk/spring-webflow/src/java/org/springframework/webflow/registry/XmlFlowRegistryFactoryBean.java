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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;

/**
 * A factory bean that produces a populated flow registry using a
 * {@link XmlFlowRegistrar}. This is the simplest implementation to use when
 * using a Spring BeanFactory to deploy an explicit registry of XML-based Flow
 * definitions for execution.
 * <p>
 * By default, a configured flow definition will be assigned a registry
 * identifier equal to the filename of the underlying definition resource, minus
 * the filename extension. For example, a XML-based flow definition defined in
 * the file <code>flow1.xml</code> will be identified as <code>flow1</code>
 * in the registry created by this factory bean.
 * <p>
 * This class is also <code>BeanFactoryAware</code> and when used with Spring
 * will automatically create a configured
 * {@link FlowRegistryFlowArtifactFactory} for loading Flow artifacts like
 * Actions from the Spring bean factory during the Flow registration process.
 * <p>
 * This class is also <code>ResourceLoaderAware</code>; when an instance is
 * created by a Spring BeanFactory the factory will automatically configure the
 * XmlFlowRegistrar with a context-relative resource loader for accessing other
 * resources during Flow assembly.
 * 
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowLocations&quot;&gt;
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
public class XmlFlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {

	/**
	 * The flow registrar that will perform the definition registrations.
	 */
	private ExternalizedFlowRegistrar flowRegistrar = createFlowRegistrar();

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
		setBeanFactory(beanFactory);
	}

	/**
	 * Factory method that returns a new externalized flow registrar. Subclasses
	 * may override.
	 * @return the flow registrar to use
	 */
	protected ExternalizedFlowRegistrar createFlowRegistrar() {
		return new XmlFlowRegistrar();
	}

	/**
	 * Returns the configured externalized flow registrar.
	 */
	protected ExternalizedFlowRegistrar getFlowRegistrar() {
		return flowRegistrar;
	}

	/**
	 * Sets the locations (resource file paths) pointing to XML-based flow
	 * definitions.
	 * <p>
	 * When configuring as a spring bean definition, ANT-style
	 * resource patterns/wildcards are also supported, taking advantage of
	 * Spring's built in ResourceArrayPropertyEditor machinery.
	 * <p>
	 * For example:
	 * <pre>
	 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
	 *         &lt;property name=&quot;flowLocations&quot;&gt; value="/WEB-INF/flows/*-flow.xml"/> 
	 *     &lt;/bean&gt;
	 * </pre>
	 * Flows registered from this set will be automatically assigned an id based
	 * on the filename of the matched XML resource.
	 * @param locations the resource locations
	 */
	public void setFlowLocations(Resource[] locations) {
		getFlowRegistrar().setFlowLocations(locations);
	}

	/**
	 * Sets the formal set of externalized XML flow definitions to be
	 * registered.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and
	 * the set of properties applied to the externalized flow resource.
	 * @param flowDefinitions the externalized flow definition specification
	 */
	public void setFlowDefinitions(ExternalizedFlowDefinition[] flowDefinitions) {
		getFlowRegistrar().setFlowDefinitions(flowDefinitions);
	}

	protected void doPopulate(FlowRegistry registry) {
		getFlowRegistrar().registerFlows(registry, getFlowArtifactFactory());
	}
}