package org.springframework.webflow.registry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;

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
public class FlowRegistryImpl implements FlowRegistry {

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions = new TreeMap();

	/**
	 * An optional parent flow registry.
	 */
	private FlowRegistry parent;

	public void setParent(FlowRegistry parent) {
		this.parent = parent;
	}

	public String[] getFlowDefinitionIds() {
		return (String[])flowDefinitions.keySet().toArray(new String[0]);
	}

	public int getFlowDefinitionCount() {
		return flowDefinitions.size();
	}

	public void registerFlowDefinition(FlowHolder flowHolder) {
		Assert.notNull(flowHolder, "The flow definition holder to register is required");
		index(flowHolder);
	}

	public boolean containsFlowDefinition(String id) {
		return flowDefinitions.get(id) != null;
	}

	public void removeFlowDefinition(String id) {
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
				FlowHolder holder = (FlowHolder)entry.getValue();
				holder.refresh();
				if (!holder.getId().equals(key)) {
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

	/**
	 * Simple value object that holds the key for an indexed flow definition
	 * holder in this registry. Used to support reindexing on a refresh.
	 * @author Keith Donald
	 */
	private static class Indexed {
		private String key;

		private FlowHolder holder;

		public Indexed(String key, FlowHolder holder) {
			this.key = key;
			this.holder = holder;
		}
	}

	public void refresh(String flowId) throws IllegalArgumentException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			try {
				FlowHolder holder = getFlowDefinitionHolder(flowId);
				holder.refresh();
				if (!holder.getId().equals(flowId)) {
					reindex(holder, flowId);
				}
			}
			catch (NoSuchFlowDefinitionException e) {
				// rethrow without context for generic JMX clients
				throw new IllegalArgumentException("Unable to complete refresh operation: "
						+ "no flow definition with id '" + flowId + "' is stored in this registry");
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	private void reindex(FlowHolder holder, String oldId) {
		flowDefinitions.remove(oldId);
		index(holder);
	}

	private void index(FlowHolder holder) {
		flowDefinitions.put(holder.getId(), holder);
	}

	private FlowHolder getFlowDefinitionHolder(String id) {
		FlowHolder flowHolder = (FlowHolder)flowDefinitions.get(id);
		if (flowHolder == null) {
			throw new NoSuchFlowDefinitionException(id);
		}
		return flowHolder;
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		try {
			return getFlowDefinitionHolder(id).getFlow();
		}
		catch (NoSuchFlowDefinitionException e) {
			if (parent != null) {
				return parent.getFlow(id);
			}
			throw e;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("flowDefinitions", flowDefinitions).append("parent", parent).toString();
	}
}