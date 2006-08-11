package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecution;

public interface FlowExecutionStateRestorer {
	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope);
}