package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A support strategy used by repositories that serialize flow executions to
 * restore transient execution state after deserialization.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionStateRestorer {

	/**
	 * Restore the transient state of the flow execution.
	 * 
	 * @param flowExecution the (potentially deserialized) flow execution
	 * @param conversationScope the execution's conversation scope, which is
	 * typically not part of the serialized form
	 * @return the restored flow execution
	 */
	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope);
}