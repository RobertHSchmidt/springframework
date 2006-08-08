package org.springframework.webflow.execution.factory;

import org.springframework.webflow.execution.FlowExecution;

public interface FlowExecutionFactory {
	public FlowExecution createFlowExecution(String flowId);

	public FlowExecution rehydrateFlowExecution(FlowExecution flowExecution);
}
