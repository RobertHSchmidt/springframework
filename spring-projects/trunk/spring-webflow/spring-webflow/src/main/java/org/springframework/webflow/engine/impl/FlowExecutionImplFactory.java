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
package org.springframework.webflow.engine.impl;

import org.springframework.util.Assert;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.execution.factory.support.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.support.StaticFlowExecutionListenerLoader;

/**
 * A factory for the {@link FlowExecutionImpl default flow execution}
 * implementation.
 * 
 * @author Keith Donald
 */
public class FlowExecutionImplFactory implements FlowExecutionFactory {

	/**
	 * The strategy for loading listeners that should observe executions of a
	 * flow definition. The default simply loads an empty static listener list.
	 */
	private FlowExecutionListenerLoader listenerLoader;

	/**
	 * Creates a new {@link FlowExecutionImpl} factory that returns new flow
	 * executions with no listeners attached.
	 */
	public FlowExecutionImplFactory() {
		this(new StaticFlowExecutionListenerLoader());
	}

	/**
	 * Creates a new {@link FlowExecutionImpl} factory; the listener loader is
	 * used to determine the specified listeners to attach to the new flow
	 * executions.
	 * @param listenerLoader the flow execution listener loader
	 */
	public FlowExecutionImplFactory(FlowExecutionListenerLoader listenerLoader) {
		setListenerLoader(listenerLoader);
	}

	/**
	 * Sets the strategy for loading listeners that should observe executions of
	 * a flow definition.
	 */
	private void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		Assert.notNull(listenerLoader, "The listener loader is required");
		this.listenerLoader = listenerLoader;
	}

	public FlowExecution createFlowExecution(FlowDefinition flowDefinition) {
		Assert.isInstanceOf(Flow.class, flowDefinition, "Flow definition is of wrong type: ");
		return new FlowExecutionImpl((Flow)flowDefinition, listenerLoader.getListeners(flowDefinition));
	}
}