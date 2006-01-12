package org.springframework.webflow.manager;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;

/**
 * The central facade entry-point into the Spring Web Flow system. This inteface
 * defines the system boundary.
 * @author Keith Donald
 */
public interface FlowExecutionManager {

	/**
	 * Adds a flow execution listener to this manager.
	 * @param listener the listener
	 * @param criteria the listener criteria
	 */
	public void addListener(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria);

	/**
	 * Removes an attached flow execution listener from this manager.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener);

	/**
	 * Launch a new execution of the flow provided in the context of the current
	 * external request.
	 * @param flowId the unique id of the flow definition to launch
	 * @param context the external context representing the state of a request
	 * into Spring Web Flow from an external system.
	 * @return the starting view selection, or <code>null</code> if no view
	 * selection was made.
	 * @throws FlowException if an exception occured launching the new flow
	 * execution.
	 */
	public ViewSelection launch(String flowId, ExternalContext context) throws FlowException;

	/**
	 * Signal an occurrence of an event in the current state of an existing,
	 * paused flow execution continuation. The flow execution will resume to
	 * process the event.
	 * @param eventId the user event that occured
	 * @param flowExecutionId the unique id of a paused flow execution
	 * continuation that is waiting to resume on the occurrence of an user
	 * event.
	 * @param context the external context representing the state of a request
	 * into Spring Web Flow from an external system.
	 * @return the next view selection, or <code>null</code> if no view
	 * selection was made.
	 * @throws FlowException if an exception occured launching the new flow
	 * execution.
	 */
	public ViewSelection signalEvent(String eventId, String flowExecutionId, ExternalContext context)
			throws FlowException;

}