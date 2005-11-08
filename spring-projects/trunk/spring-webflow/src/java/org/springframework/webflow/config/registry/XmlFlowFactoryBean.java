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
package org.springframework.webflow.config.registry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.config.BeanFactoryFlowArtifactLocator;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * A flow factory bean that assembles a Flow from a XML resource location, for
 * convenient use with a Spring bean factory.
 * 
 * Usage example:
 * 
 * <pre>
 *    &lt;bean id=&quot;myFlow&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowFactoryBean&quot;&gt;
 *        &lt;constructor-arg value=&quot;classpath:example/myflow.xml&quot;/&gt;
 *    &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class XmlFlowFactoryBean extends FlowFactoryBean implements BeanFactoryAware {

	/**
	 * Creates a new XML flow factory bean.
	 */
	public XmlFlowFactoryBean() {
		this(new DescriptiveResource("No 'location' property set"));
	}

	/**
	 * Creates a new XML flow factory bean.
	 * @param location the location of the xml resource.
	 */
	public XmlFlowFactoryBean(Resource location) {
		super(new XmlFlowBuilder(location));
	}

	/**
	 * Returns the XML flow builder used by this factory bean.
	 */
	protected XmlFlowBuilder getXmlFlowBuilder() {
		return (XmlFlowBuilder)getFlowBuilder();
	}

	/**
	 * Set the location of the xml flow definition resource.
	 * @param location the flow definition resource
	 */
	public void setLocation(Resource location) {
		getXmlFlowBuilder().setLocation(location);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		getXmlFlowBuilder().setFlowArtifactLocator(new BeanFactoryFlowArtifactLocator(beanFactory));
	}
}