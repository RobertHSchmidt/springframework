package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.Stack;

public class FlowExecutionStack {
	private Serializable key;

	private Stack snapshots;

	public FlowExecutionStack(Serializable key) {
		this.key = key;
	}
}
