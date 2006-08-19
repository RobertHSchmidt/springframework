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
package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * Unit tests for the SwfNamespaceHandler and its BeanDefinitionParsers
 * 
 * @author Ben Hale
 */
public class FlowNamespaceHandlerTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		super.setUp();
		this.beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource("org/springframework/webflow/config/testFlowNamespace.xml"));
	}

	public void testRegistryWithPath() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)this.beanFactory.getBean("withPath");
		assertEquals("Incorrect number of flows loaded", 1, registry.getFlowDefinitionCount());
	}

	public void testRegistryWithoutPath() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)this.beanFactory.getBean("withoutPath");
		assertEquals("Incorrect number of flows loaded", 0, registry.getFlowDefinitionCount());
	}

	public void testRegistryWithPathWithWildcards() {
		FlowDefinitionRegistry registry = (FlowDefinitionRegistry)this.beanFactory.getBean("withPathWithWildcards");
		assertEquals("Incorrect number of flows loaded", 0, registry.getFlowDefinitionCount());
	}

	public void testExecutorCreation() {
		FlowExecutor flowExecutor = (FlowExecutor)this.beanFactory.getBean("defaultExecutor");
		assertNotNull("Incorrect FlowExecutor creation", flowExecutor);
	}
}