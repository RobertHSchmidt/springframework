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
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.XmlFlowBuilder;

/**
 * Convenient specialization of FlowFactoryBean that uses an XmlFlowBuilder to
 * build flows from an XML file. The XML file to load can be specified directly
 * using the "location" property.
 * 
 * @see org.springframework.webflow.config.XmlFlowBuilder
 * @see org.springframework.webflow.registry.FlowFactoryBean
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class XmlFlowFactoryBean extends FlowFactoryBean implements BeanFactoryAware {

	/**
	 * Creates an XML flow factory bean.
	 */
	public XmlFlowFactoryBean() {
		super(new XmlFlowBuilder(new DescriptiveResource("Not yet set")));
	}

	/**
	 * Set the resource from which an XML flow definition will be read.
	 * @param location the resource location
	 */
	public void setLocation(Resource location) {
		getXmlFlowBuilder().setLocation(location);
	}

	public void setFlowBuilder(FlowBuilder flowBuilder) {
		Assert.isInstanceOf(XmlFlowBuilder.class, flowBuilder);
		super.setFlowBuilder(flowBuilder);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		getXmlFlowBuilder().setFlowArtifactFactory(new BeanFactoryFlowArtifactFactory(beanFactory));
	}

	/**
	 * Returns the XML based flow builder used by this factory bean.
	 */
	protected XmlFlowBuilder getXmlFlowBuilder() {
		return (XmlFlowBuilder)getFlowBuilder();
	}
}