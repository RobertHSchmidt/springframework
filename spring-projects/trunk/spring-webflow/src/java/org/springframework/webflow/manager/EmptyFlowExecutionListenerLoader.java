/**
 * 
 */
package org.springframework.webflow.manager;

import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;

public class EmptyFlowExecutionListenerLoader implements FlowExecutionListenerLoader {

	private static final FlowExecutionListener[] EMPTY_LISTENER_ARRAY = new FlowExecutionListener[0];

	public FlowExecutionListener[] getListeners(Flow flow) {
		return EMPTY_LISTENER_ARRAY;
	}
}