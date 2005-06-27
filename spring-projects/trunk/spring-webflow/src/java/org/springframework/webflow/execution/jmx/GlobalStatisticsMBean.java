package org.springframework.webflow.execution.jmx;

/**
 * A JMX management interface for global statistics on the web flow system.
 * @author Keith Donald
 */
public interface GlobalStatisticsMBean {
	
	/**
	 * Returns the total number of flow executions that have been created since this
	 * system starting serving requests.
	 * @return the total
	 */
	public int getTotalFlowExecutionCount();

	/**
	 * Returns the total number of requests into the webflow system.
	 * @return the total
	 */
	public int getTotalRequestCount();

	/**
	 * Returns the number of requests currently being processed by the webflow system.
	 * @return the requests in process count
	 */
	public int getRequestsInProcessCount();
	
	/**
	 * Gets the current number of managed flow executions - managed executions may be in a 
	 * active or paused state, but they exist (have not been ended.)
	 * @return the managed count
	 */
	public int getManagedFlowExecutionCount();

	/**
	 * Returns the current number of paused flow executions.  Paused flows are waiting on the user
	 * to do something.
	 * @return the paused count
	 */
	public int getPausedFlowExecutionCount();

	/**
	 * Returns the current number of active flow executions.  Active flows are doing work -- the user is
	 * waiting on the flow to come back with a response.
	 * @return the active count
	 */
	public int getActiveFlowExecutionCount();
	
	/**
	 * Returns the number of flow executions that were created that have ended normally.
	 * @return the ended count
	 */
	public int getEndedFlowExecutionCount();
	
	/**
	 * Are global statistics enabled?
	 * @return true if yes, false otherwise
	 */
	public boolean isStatisticsEnabled();
	
	/**
	 * Turn statistics collection on/off
	 * @param enabled true to turn on, false to turn off
	 */
	public void setStatisticsEnabled(boolean statisticsEnabled);
	
	/**
	 * Reset these stats.
	 */
	public void reset();
}
