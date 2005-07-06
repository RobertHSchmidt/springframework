package org.springframework.webflow.execution.jmx;

import org.springframework.webflow.execution.FlowExecution;

/**
 * Adapts a strongly typed flow execution to a more generically typed flow execution mbean
 * interface for purposes of enabled standards-based jmx management.
 * @author Keith Donald
 */
public class FlowExecutionMBeanAdapter implements FlowExecutionMBean {

	/**
	 * The wrapped flow execution being exported for management.
	 */
	private FlowExecution flowExecution;
	
	public FlowExecutionMBeanAdapter(FlowExecution flowExecution) {
		this.flowExecution = flowExecution;
	}
	
	public String getRootFlow() {
		return flowExecution.getRootFlow().getId();
	}

	public String getActiveFlow() throws IllegalStateException {
		return flowExecution.getActiveFlow().getId();
	}

	public String getCurrentState() throws IllegalStateException {
		return flowExecution.getCurrentState().getId();
	}

	public int getStatus() {
		return flowExecution.getActiveSession().getStatus().getShortCode();
	}

	public String getCaption() {
		return flowExecution.getCaption();
	}
	
	public String getKey() {
		return flowExecution.getKey();
	}

	public long getCreationTimestamp() {
		return flowExecution.getCreationTimestamp();
	}

	public long getUptime() {
		return flowExecution.getUptime();
	}

	public long getLastRequestTimestamp() {
		return flowExecution.getLastRequestTimestamp();
	}

	public String getLastEventId() {
		return flowExecution.getLastEventId();
	}

	public boolean isActive() {
		return flowExecution.isActive();
	}

	public boolean isRootFlowActive() {
		return flowExecution.isRootFlowActive();
	}
}