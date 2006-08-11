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
package org.springframework.webflow.definition.registry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.FlowDefinitionHolder;

/**
 * A generic registry of one or more Flow definitions.
 * <p>
 * This registry may be refreshed at runtime to "hot reload" refreshable Flow
 * definitions.
 * <p>
 * This registry be configured with a "parent" flow registry to provide a hook
 * into a larger flow definition registry hierarchy.
 * 
 * @author Keith Donald
 */
public class FlowDefinitionRegistryImpl implements FlowDefinitionRegistry {

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions = new TreeMap();

	/**
	 * An optional parent flow registry.
	 */
	private FlowDefinitionRegistry parent;

	/**
	 * Sets this registry's parent registry. When asked by a client to locate a
	 * flow definition this registry will query it's parent if it cannot
	 * fullfill the lookup request.
	 * @param parent the parent flow registry, may be null
	 */
	public void setParent(FlowDefinitionRegistry parent) {
		this.parent = parent;
	}

	public String[] getFlowDefinitionIds() {
		return (String[])flowDefinitions.keySet().toArray(new String[flowDefinitions.size()]);
	}

	public int getFlowDefinitionCount() {
		return flowDefinitions.size();
	}

	public boolean containsFlowDefinition(String id) {
		Assert.hasText(id, "The flow id is required");
		return flowDefinitions.get(id) != null;
	}

	public FlowDefinition[] getFlowDefinitions() {
		FlowDefinition[] flows = new FlowDefinition[flowDefinitions.size()];
		Iterator it = flowDefinitions.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			FlowDefinitionHolder holder = (FlowDefinitionHolder)it.next();
			flows[i] = holder.getFlowDefinition();
			i++;
		}
		return flows;
	}

	public void registerFlowDefinition(FlowDefinitionHolder flowHolder) {
		Assert.notNull(flowHolder, "The flow definition holder to register is required");
		index(flowHolder);
	}

	public void removeFlowDefinition(String id) {
		Assert.hasText(id, "The flow id is required");
		flowDefinitions.remove(id);
	}

	public void refresh() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			LinkedList needsReindexing = new LinkedList();
			Iterator it = flowDefinitions.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String key = (String)entry.getKey();
				FlowDefinitionHolder holder = (FlowDefinitionHolder)entry.getValue();
				holder.refresh();
				if (!holder.getFlowDefinitionId().equals(key)) {
					needsReindexing.add(new Indexed(key, holder));
				}
			}
			it = needsReindexing.iterator();
			while (it.hasNext()) {
				Indexed indexed = (Indexed)it.next();
				reindex(indexed.holder, indexed.key);
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	public void refresh(String flowId) throws NoSuchFlowDefinitionException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			FlowDefinitionHolder holder = getFlowDefinitionHolder(flowId);
			holder.refresh();
			if (!holder.getFlowDefinitionId().equals(flowId)) {
				reindex(holder, flowId);
			}

		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	private void reindex(FlowDefinitionHolder holder, String oldId) {
		flowDefinitions.remove(oldId);
		index(holder);
	}

	private void index(FlowDefinitionHolder holder) {
		Assert.hasText(holder.getFlowDefinitionId(), "The flow holder to index must return a non-blank flow id");
		flowDefinitions.put(holder.getFlowDefinitionId(), holder);
	}

	private FlowDefinitionHolder getFlowDefinitionHolder(String id) {
		FlowDefinitionHolder flowHolder = (FlowDefinitionHolder)flowDefinitions.get(id);
		if (flowHolder == null) {
			throw new NoSuchFlowDefinitionException(id, getFlowDefinitionIds());
		}
		return flowHolder;
	}

	// implementing FlowLocator

	public FlowDefinition getFlowDefinition(String id) throws NoSuchFlowDefinitionException {
		Assert.hasText(id,
				"Unable to load a flow definition: no flow id was provided.  Please provide a valid flow identifier.");
		try {
			return getFlowDefinitionHolder(id).getFlowDefinition();
		}
		catch (NoSuchFlowDefinitionException e) {
			if (parent != null) {
				// try parent
				return parent.getFlowDefinition(id);
			}
			throw e;
		}
	}

	/**
	 * Simple value object that holds the key for an indexed flow definition
	 * holder in this registry. Used to support reindexing on a refresh.
	 * @author Keith Donald
	 */
	private static class Indexed {
		private String key;

		private FlowDefinitionHolder holder;

		public Indexed(String key, FlowDefinitionHolder holder) {
			this.key = key;
			this.holder = holder;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("flowDefinitions", flowDefinitions).append("parent", parent).toString();
	}
}