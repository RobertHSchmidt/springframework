package org.springframework.webflow.execution;

import org.springframework.webflow.Flow;

/**
 * A strategy interface for loading a set of FlowExecutionListener's
 * for a provided flow definition.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionListenerLoader {
	
	/**
	 * Get the flow execution listeners that apply to given flow definition.
	 * @param flow the flow definition
	 * @return the listeners that apply
	 */
	public FlowExecutionListener[] getListeners(Flow flow);
}
