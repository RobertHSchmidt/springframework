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
package org.springframework.webflow.engine.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;

/**
 * A factory for instances of the {@link FlowExecutionImpl default flow execution} implementation.
 * @author Keith Donald
 */
public class FlowExecutionImplFactory extends FlowExecutionImplServicesConfigurer implements FlowExecutionFactory {

	private static final Log logger = LogFactory.getLog(FlowExecutionImplFactory.class);

	public FlowExecution createFlowExecution(FlowDefinition flowDefinition) {
		Assert.isInstanceOf(Flow.class, flowDefinition, "Flow definition is of wrong type: ");
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new execution of '" + flowDefinition.getId() + "'");
		}
		return configureServices(new FlowExecutionImpl((Flow) flowDefinition));
	}
}