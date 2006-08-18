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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * @author Keith Donald
 */
public abstract class FlowExecutorFactoryBean extends DefaultFlowExecutorFactory implements FactoryBean {

	public FlowExecutorFactoryBean(FlowDefinitionLocator definitionLocator) {
		super(definitionLocator);
	}

	public Class getObjectType() {
		return FlowExecutor.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return createFlowExecutor();
	}
}