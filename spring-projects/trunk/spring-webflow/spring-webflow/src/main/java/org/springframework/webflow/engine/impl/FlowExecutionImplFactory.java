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
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;

/**
 * A factory for instances of the
 * {@link FlowExecutionImpl default flow execution} implementation.
 * 
 * @author Keith Donald
 */
public class FlowExecutionImplFactory implements FlowExecutionFactory {

	/**
	 * The strategy for loading listeners that should observe executions of a
	 * flow definition. The default simply loads an empty static listener list.
	 */
	private FlowExecutionListenerLoader executionListenerLoader = StaticFlowExecutionListenerLoader.EMPTY_INSTANCE;

	/**
	 * System execution attributes that may influence flow execution behavior.
	 */
	private AttributeMap executionAttributes = CollectionUtils.EMPTY_ATTRIBUTE_MAP;

	/**
	 * Sets the strategy for loading listeners that should observe executions of
	 * a flow definition. Allows full control over what listeners should apply
	 * for executions of a flow definition.
	 */
	public void setExecutionListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		Assert.notNull(listenerLoader, "The listener loader is required");
		this.executionListenerLoader = listenerLoader;
	}

	/**
	 * Sets the attributes to apply to flow executions created by this factory.
	 * Execution attributes may affect flow execution behavior.
	 * @param attributes flow execution system attributes
	 */
	public void setExecutionAttributes(AttributeMap attributes) {
		this.executionAttributes = attributes;
	}

	/**
	 * Convenience setter for setting a single listener that always applys to
	 * flow executions created by this factory.
	 * @param listener the flow execution listener
	 */
	public void setLExecutionistener(FlowExecutionListener listener) {
		setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(listener));
	}

	/**
	 * Convenience setter for setting a list of listeners that always apply to
	 * flow executions created by this factory.
	 * @param listeners the flow execution listeners
	 */
	public void setExecutionListeners(FlowExecutionListener[] listeners) {
		setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(listeners));
	}

	public FlowExecution createFlowExecution(FlowDefinition flowDefinition) {
		Assert.isInstanceOf(Flow.class, flowDefinition, "Flow definition is of wrong type: ");
		return new FlowExecutionImpl((Flow)flowDefinition, executionListenerLoader.getListeners(flowDefinition), executionAttributes);
	}
}