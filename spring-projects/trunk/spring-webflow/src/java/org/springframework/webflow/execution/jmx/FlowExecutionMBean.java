package org.springframework.webflow.execution.jmx;

import org.springframework.webflow.FlowExecutionStatistics;

public interface FlowExecutionMBean extends FlowExecutionStatistics {
	
	/**
	 * Returns the root (top-level) flow.
	 * @return the root flow id
	 */
	public String getRootFlow();

	/**
	 * Returns the current active flow.
	 * @return the active flow id
	 */
	public String getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the current state of the active flow.
	 * @return the current state id
	 */
	public String getCurrentState() throws IllegalStateException;

	/**
	 * Returns the current status of this flow execution.
	 * @return the status code
	 */
	public int getStatus();
}