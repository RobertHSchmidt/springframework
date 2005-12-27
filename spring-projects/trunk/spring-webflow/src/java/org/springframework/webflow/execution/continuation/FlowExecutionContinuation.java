package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

public interface FlowExecutionContinuation extends Serializable {
	public Serializable getId();

	public FlowExecution getFlowExecution();
}