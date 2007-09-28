package org.springframework.webflow.execution;


/**
 * A factory for creating flow execution keys. Used to generate a persistent identity for a flow execution that needs to
 * be persisted.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionKeyFactory {

	/**
	 * Get the key to assign to the flow execution. This factory simply generates the key to assign, it does not
	 * actually perform the key assignment.
	 * 
	 * @param execution the flow execution
	 * @return the key to assign to the flow execution
	 */
	public FlowExecutionKey getKey(FlowExecution execution);
}
