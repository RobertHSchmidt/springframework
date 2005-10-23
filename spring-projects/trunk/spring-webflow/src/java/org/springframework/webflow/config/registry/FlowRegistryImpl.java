package org.springframework.webflow.config.registry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.access.FlowArtifactLookupException;
import org.springframework.webflow.access.NoSuchFlowDefinitionException;

/**
 * A generic registry of Flow definitions. May also be refreshed at runtime to
 * support "hot reloading" of refreshable Flow definitions.
 * 
 * @author Keith Donald
 */
public class FlowRegistryImpl implements FlowRegistry {

	/**
	 * The map of loaded Flow definitions maintained in this registry.
	 */
	private Map flowDefinitions = new TreeMap();

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
			// @TODO workaround for JMX
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

	public void refresh(String id) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// @TODO workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			FlowDefinitionHolder holder = getFlowDefinitionHolder(id);
			holder.refresh();
			if (!holder.getId().equals(id)) {
				reindex(holder, id);
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

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		return getFlowDefinitionHolder(id).getFlow();
	}

	public String toString() {
		return new ToStringCreator(this).append("flowDefinitions", flowDefinitions).toString();
	}
}