package org.springframework.webflow.test;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;

public class MockFlowExecutionKeyFactory implements FlowExecutionKeyFactory {
	public FlowExecutionKey getKey(FlowExecution execution) {
		return new MockFlowExecutionKey();
	}
}
