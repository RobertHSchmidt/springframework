package org.springframework.webflow.execution.factory;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecution;

/**
 * An abstract factory for creating flow exections. A flow execution represents
 * a runtime, top-level instance of a flow definition.
 * <p>
 * This factory provides encapsulation of the flow execution implementation
 * type, as well as its construction and assembly process.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionFactory {

	/**
	 * Create a new flow execution product for the flow definition.
	 * @param flowDefinition the flow definition
	 * @return the new flow execution, fully initialized and awaiting to be
	 * started.
	 */
	public FlowExecution createFlowExecution(FlowDefinition flowDefinition);
}
