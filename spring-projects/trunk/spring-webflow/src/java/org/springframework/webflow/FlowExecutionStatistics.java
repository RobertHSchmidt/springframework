package org.springframework.webflow;

/**
 * A generically typed flow execution statistics interface for use by management clients.
 * These stats would typically be exported for management via JMX.  References to strongly-typed
 * webflow classes (e.g Flow, State) should not go here -- put them in the FlowExecutionContext
 * subinterface.
 *
 * @see org.springframework.webflow.FlowExecutionContext
 *  
 * @author Keith Donald
 */
public interface FlowExecutionStatistics {
	
	/**
	 * Returns a display string suitable for logging/printing in a console
	 * containing info about this executing flow.
	 * @return the flow execution caption
	 */
	public String getCaption();

	/**
	 * Returns the time at which this flow started executing.
	 * @return the creation timestamp
	 */
	public long getCreationTimestamp();

	/**
	 * Returns the time in milliseconds this flow execution has been active.
	 * @return the flow execution up time
	 */
	public long getUptime();

	/**
	 * Returns the timestamp noting when the last request to manipulate this
	 * executing flow was received.
	 * @return the timestamp of the last client request
	 */
	public long getLastRequestTimestamp();

	/**
	 * Returns the id of the last event that occured in this executing flow.
	 * @return the last event id
	 */
	public String getLastEventId();

	/**
	 * Is the flow execution active?
	 * @return true if active, false if flow execution has terminated
	 */
	public boolean isActive();
	
	/**
	 * Is the root flow of the flow execution currently active?
	 * @return true if so, false otherwise
	 */
	public boolean isRootFlowActive();
}