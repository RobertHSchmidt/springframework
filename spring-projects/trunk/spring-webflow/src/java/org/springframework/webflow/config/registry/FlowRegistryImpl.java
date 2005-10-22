package org.springframework.webflow.config.registry;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
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
		this.flowDefinitions.put(flowHolder.getFlowId(), flowHolder);
	}

	public void refresh() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// @TODO workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			Iterator it = flowDefinitions.values().iterator();
			while (it.hasNext()) {
				((FlowDefinitionHolder)it.next()).refresh();
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	public void refresh(String flowId) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// @TODO workaround for JMX
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			getFlowDefinitionHolder(flowId).refresh();
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
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