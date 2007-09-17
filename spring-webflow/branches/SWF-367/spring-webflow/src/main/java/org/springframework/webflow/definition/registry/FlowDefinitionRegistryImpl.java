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
package org.springframework.webflow.definition.registry;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.FlowId;

/**
 * A generic registry implementation for housing one or more flow definitions.
 * 
 * @author Keith Donald
 */
public class FlowDefinitionRegistryImpl implements FlowDefinitionRegistry {

	private static final Log logger = LogFactory.getLog(FlowDefinitionRegistryImpl.class);

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions;

	/**
	 * An optional parent flow definition registry.
	 */
	private FlowDefinitionRegistry parent;

	public FlowDefinitionRegistryImpl() {
		flowDefinitions = new TreeMap();
	}

	// implementing FlowDefinitionLocator

	public FlowDefinition getFlowDefinition(FlowId id) throws NoSuchFlowDefinitionException,
			FlowDefinitionConstructionException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting flow definition with id '" + id + "'");
			}
			return getFlowDefinitionHolder(id).getFlowDefinition();
		} catch (NoSuchFlowDefinitionException e) {
			if (parent != null) {
				// try parent
				return parent.getFlowDefinition(id);
			}
			throw e;
		}
	}

	// implementing FlowDefinitionRegistry

	public void setParent(FlowDefinitionRegistry parent) {
		this.parent = parent;
	}

	public void registerFlowDefinition(FlowDefinitionHolder flowHolder) {
		Assert.notNull(flowHolder, "The holder of the flow definition to register is required");
		FlowId id = flowHolder.getFlowDefinitionId();
		Map namespace = getNamespace(id.getNamespace());
		namespace.put(id.getShortName(), flowHolder);
	}

	// internal helpers

	/**
	 * Returns the identified flow definition holder. Throws an exception if it cannot be found.
	 */
	private FlowDefinitionHolder getFlowDefinitionHolder(FlowId id) throws NoSuchFlowDefinitionException {
		Map namespace = getNamespace(id.getNamespace());
		FlowDefinitionHolder flowHolder = (FlowDefinitionHolder) namespace.get(id.getShortName());
		if (flowHolder == null) {
			throw new NoSuchFlowDefinitionException(id);
		}
		return flowHolder;
	}

	/**
	 * Returns the map for a given namespace. Creates the map if it does not exist.
	 */
	private Map getNamespace(String namespace) {
		if (!flowDefinitions.containsKey(namespace)) {
			flowDefinitions.put(namespace, new TreeMap());
		}
		return (Map) flowDefinitions.get(namespace);
	}

	public String toString() {
		return new ToStringCreator(this).append("flowDefinitions", flowDefinitions).append("parent", parent).toString();
	}
}