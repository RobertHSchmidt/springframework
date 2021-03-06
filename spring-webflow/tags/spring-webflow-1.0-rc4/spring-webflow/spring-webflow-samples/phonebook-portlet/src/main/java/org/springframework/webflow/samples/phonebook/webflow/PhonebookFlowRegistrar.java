/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.samples.phonebook.webflow;

import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistrar;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.StaticFlowDefinitionHolder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowServiceLocator;

/**
 * Demonstrates how to register flows programatically.
 * 
 * @author Keith Donald
 */
public class PhonebookFlowRegistrar implements FlowDefinitionRegistrar {
	private FlowServiceLocator serviceLocator;

	public PhonebookFlowRegistrar(FlowServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	public void registerFlowDefinitions(FlowDefinitionRegistry registry) {
		registry.registerFlowDefinition(assemble("search-flow", new SearchPersonFlowBuilder(serviceLocator)));
		registry.registerFlowDefinition(assemble("detail-flow", new PersonDetailFlowBuilder(serviceLocator)));
	}

	private FlowDefinitionHolder assemble(String flowId, FlowBuilder flowBuilder) {
		return new StaticFlowDefinitionHolder(new FlowAssembler(flowId, flowBuilder).assembleFlow());
	}
}