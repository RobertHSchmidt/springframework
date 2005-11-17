package org.springframework.webflow.config.registry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;

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

	public void registerFlowDefinition(FlowDefinitionHolder flowHolder) {
		Assert.notNull(flowHolder, "The flow definition holder to register is required");
		index(flowHolder);
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

		private FlowDefinitionHolder holder;

		public Indexed(String key, FlowDefinitionHolder holder) {
			this.key = key;
			this.holder = holder;
		}
	}

	public void refresh(String id) throws IllegalArgumentException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			try {
				FlowDefinitionHolder holder = getFlowDefinitionHolder(id);
				holder.refresh();
				if (!holder.getId().equals(id)) {
					reindex(holder, id);
				}
			}
			catch (NoSuchFlowDefinitionException e) {
				// rethrow without context for generic JMX clients
				throw new IllegalArgumentException("Unable to complete refresh operation: "
						+ "no flow definition with id '" + id + "' is stored in this registry");
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
		flowDefinitions.put(holder.getId(), holder);
	}

	private FlowDefinitionHolder getFlowDefinitionHolder(String id) {
		FlowDefinitionHolder flowHolder = (FlowDefinitionHolder)flowDefinitions.get(id);
		if (flowHolder == null) {
			throw new NoSuchFlowDefinitionException(id);
		}
		return flowHolder;
	}

	public boolean containsFlow(String id) {
		return flowDefinitions.get(id) != null;
	}

	public Flow getFlow(String id) throws FlowArtifactException {
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